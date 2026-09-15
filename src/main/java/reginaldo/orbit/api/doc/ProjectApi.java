package reginaldo.orbit.api.doc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import reginaldo.orbit.api.dto.project.ProjectRequest;
import reginaldo.orbit.api.dto.project.ProjectResponse;

import java.util.List;
import java.util.UUID;

@Tag(name = OpenApiConfig.TAG_PROJECTS)
public interface ProjectApi {

    @Operation(summary = "Criar projeto",
            description = "Cria um projeto para o usuário autenticado.")
    ResponseEntity<ProjectResponse> create(ProjectRequest request, Authentication authentication);

    @Operation(summary = "Listar projetos",
            description = "Lista somente os projetos do usuário autenticado.")
    ResponseEntity<List<ProjectResponse>> list(Authentication authentication);

    @Operation(summary = "Buscar projeto",
            description = "Retorna um projeto do usuário autenticado. 404 se inexistente ou de outro usuário.")
    ResponseEntity<ProjectResponse> getById(UUID id, Authentication authentication);

    @Operation(summary = "Atualizar projeto",
            description = "Atualiza um projeto próprio.")
    ResponseEntity<ProjectResponse> update(UUID id, ProjectRequest request, Authentication authentication);

    @Operation(summary = "Excluir projeto",
            description = "Exclui um projeto próprio. 409 se o projeto possuir tarefas.")
    ResponseEntity<Void> delete(UUID id, Authentication authentication);
}
