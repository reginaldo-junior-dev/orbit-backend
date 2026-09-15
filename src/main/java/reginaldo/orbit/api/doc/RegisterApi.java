package reginaldo.orbit.api.doc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import reginaldo.orbit.api.dto.user.RegisterRequest;
import reginaldo.orbit.api.dto.user.RegisterResponse;

@Tag(name = OpenApiConfig.TAG_AUTENTICACAO)
public interface RegisterApi {

    @Operation(summary = "Registrar usuário",
            description = "Cria uma conta com role USER. Email deve ser único e a senha é armazenada com BCrypt.")
    ResponseEntity<RegisterResponse> register(RegisterRequest registerRequest);
}
