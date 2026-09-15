package reginaldo.orbit.api.doc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import reginaldo.orbit.api.dto.dashboard.DashboardResponse;

@Tag(name = OpenApiConfig.TAG_DASHBOARD)
public interface DashboardApi {

    @Operation(summary = "Dashboard",
            description = "Visão agregada com contagens de tarefas, projetos e goals do usuário autenticado.")
    ResponseEntity<DashboardResponse> getDashboard(Authentication authentication);
}
