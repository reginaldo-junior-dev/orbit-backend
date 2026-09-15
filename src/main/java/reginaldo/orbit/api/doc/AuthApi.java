package reginaldo.orbit.api.doc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import reginaldo.orbit.api.dto.auth.LoginRequest;
import reginaldo.orbit.api.dto.auth.LoginResponse;

@Tag(name = OpenApiConfig.TAG_AUTENTICACAO)
public interface AuthApi {

    @Operation(summary = "Login",
            description = "Authenticates the user and sets a JWT in an httpOnly cookie valid for 1 hour.")
    ResponseEntity<LoginResponse> login(LoginRequest loginRequest, HttpServletResponse response);
}
