package reginaldo.orbit.api.doc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import reginaldo.orbit.api.dto.user.ChangePasswordRequest;
import reginaldo.orbit.api.dto.user.UpdateUserRequest;
import reginaldo.orbit.api.dto.user.UserResponse;

@Tag(name = OpenApiConfig.TAG_USUARIO)
public interface UserApi {

    @Operation(summary = "Buscar perfil",
            description = "Retorna os dados públicos do usuário autenticado (nunca a senha).")
    ResponseEntity<UserResponse> me(Authentication authentication);

    @Operation(summary = "Atualizar perfil",
            description = "Altera nome e email do usuário autenticado. Email alterado deve continuar único (409 em conflito).")
    ResponseEntity<UserResponse> updateMe(UpdateUserRequest request, Authentication authentication);

    @Operation(summary = "Alterar senha",
            description = "Altera a senha exigindo a senha atual (400 se incorreta). A nova senha é armazenada com BCrypt.")
    ResponseEntity<Void> changePassword(ChangePasswordRequest request, Authentication authentication);

    @Operation(summary = "Excluir conta",
            description = "Exclui a conta e todos os dados do usuário (tarefas, projetos e goals).")
    ResponseEntity<Void> deleteMe(Authentication authentication);
}
