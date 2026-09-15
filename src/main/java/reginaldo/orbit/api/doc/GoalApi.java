package reginaldo.orbit.api.doc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import reginaldo.orbit.api.dto.goal.GoalRequest;
import reginaldo.orbit.api.dto.goal.GoalResponse;

import java.util.List;
import java.util.UUID;

@Tag(name = OpenApiConfig.TAG_GOALS)
public interface GoalApi {

    @Operation(summary = "Criar goal",
            description = "Cria um objetivo para o usuário autenticado.")
    ResponseEntity<GoalResponse> create(GoalRequest request, Authentication authentication);

    @Operation(summary = "Listar goals",
            description = "Lista somente os objetivos do usuário autenticado.")
    ResponseEntity<List<GoalResponse>> list(Authentication authentication);

    @Operation(summary = "Buscar goal",
            description = "Retorna um objetivo do usuário autenticado. 404 se inexistente ou de outro usuário.")
    ResponseEntity<GoalResponse> getById(UUID id, Authentication authentication);

    @Operation(summary = "Atualizar goal",
            description = "Atualiza um objetivo próprio.")
    ResponseEntity<GoalResponse> update(UUID id, GoalRequest request, Authentication authentication);

    @Operation(summary = "Excluir goal",
            description = "Exclui um objetivo próprio.")
    ResponseEntity<Void> delete(UUID id, Authentication authentication);
}
