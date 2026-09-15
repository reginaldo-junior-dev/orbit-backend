package reginaldo.orbit.api.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import reginaldo.orbit.api.doc.AuthApi;
import reginaldo.orbit.api.dto.auth.LoginRequest;
import reginaldo.orbit.api.dto.auth.LoginResponse;
import reginaldo.orbit.api.security.LoginRateLimiter;
import reginaldo.orbit.api.security.UserDetailsImpl;
import reginaldo.orbit.api.service.JwtService;

import java.time.Duration;

@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    public static final String AUTH_COOKIE_NAME = "orbit_token";

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final LoginRateLimiter loginRateLimiter;

    @Value("${COOKIE_SECURE:false}")
    private boolean cookieSecure;

    @Value("${COOKIE_SAME_SITE:Strict}")
    private String cookieSameSite;

    @PostMapping
    @Override
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest,
                                               HttpServletResponse response) {
        String email = loginRequest.email();
        loginRateLimiter.checkAllowed(email);

        UserDetailsImpl userDetails;
        try {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    email,
                    loginRequest.password()
            );
            Authentication newAuthentication = authenticationManager.authenticate(authentication);
            userDetails = (UserDetailsImpl) newAuthentication.getPrincipal();
        } catch (AuthenticationException exception) {
            loginRateLimiter.recordFailure(email);
            throw exception;
        }
        loginRateLimiter.reset(email);

        String token = jwtService.gerarToken(userDetails.getUsername(), userDetails.getId());

        ResponseCookie cookie = ResponseCookie.from(AUTH_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(Duration.ofHours(1))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(new LoginResponse("Login successful"));
    }
}
