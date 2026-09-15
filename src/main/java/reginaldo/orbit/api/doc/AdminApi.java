package reginaldo.orbit.api.doc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import reginaldo.orbit.api.dto.admin.AdminUserResponse;

import java.util.List;
import java.util.UUID;

@Tag(name = OpenApiConfig.TAG_ADMIN)
public interface AdminApi {

    @Operation(summary = "Listar usuários (ADMIN)",
            description = "Lista todos os usuários da plataforma. Apenas ADMIN (403 para USER).")
    ResponseEntity<List<AdminUserResponse>> listUsers();

    @Operation(summary = "Buscar usuário (ADMIN)",
            description = "Busca um usuário por id. Apenas ADMIN (403 para USER). Nunca retorna a senha.")
    ResponseEntity<AdminUserResponse> getUserById(UUID id);
}
