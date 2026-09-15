package reginaldo.orbit.api.doc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import reginaldo.orbit.api.dto.task.TaskRequest;
import reginaldo.orbit.api.dto.task.TaskResponse;

import java.util.List;
import java.util.UUID;

@Tag(name = OpenApiConfig.TAG_TASKS)
public interface TaskApi {

    @Operation(summary = "Criar tarefa",
            description = "Cria uma tarefa para o usuário autenticado. Pode associar a um projeto próprio (404 se o projeto for de outro usuário).")
    ResponseEntity<TaskResponse> create(TaskRequest request, Authentication authentication);

    @Operation(summary = "Listar tarefas",
            description = "Lista somente as tarefas do usuário autenticado.")
    ResponseEntity<List<TaskResponse>> list(Authentication authentication);

    @Operation(summary = "Buscar tarefa",
            description = "Retorna uma tarefa do usuário autenticado. 404 se inexistente ou de outro usuário.")
    ResponseEntity<TaskResponse> getById(UUID id, Authentication authentication);

    @Operation(summary = "Atualizar tarefa",
            description = "Atualiza uma tarefa própria. Omitir projectId desassocia a tarefa do projeto.")
    ResponseEntity<TaskResponse> update(UUID id, TaskRequest request, Authentication authentication);

    @Operation(summary = "Excluir tarefa",
            description = "Exclui uma tarefa própria. 404 se inexistente ou de outro usuário.")
    ResponseEntity<Void> delete(UUID id, Authentication authentication);
}
