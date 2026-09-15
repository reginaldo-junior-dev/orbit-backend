package reginaldo.orbit.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reginaldo.orbit.api.doc.RegisterApi;
import reginaldo.orbit.api.dto.user.RegisterRequest;
import reginaldo.orbit.api.dto.user.RegisterResponse;
import reginaldo.orbit.api.entity.User;
import reginaldo.orbit.api.service.RegisterService;

@RestController
@RequestMapping("/register")
@RequiredArgsConstructor
public class RegisterController implements RegisterApi {
    private final RegisterService registerService;

    @PostMapping
    @Override
    public ResponseEntity<RegisterResponse> register (@Valid @RequestBody RegisterRequest registerRequest) {
       RegisterResponse registerResponse = registerService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(registerResponse);
    }

}
