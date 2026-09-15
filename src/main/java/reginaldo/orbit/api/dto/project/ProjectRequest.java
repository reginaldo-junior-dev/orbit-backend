package reginaldo.orbit.api.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import reginaldo.orbit.api.enums.ProjectStatus;

public record ProjectRequest(
        @NotBlank(message = "Name is required")
        String name,
        String description,
        @NotNull(message = "Status is required")
        ProjectStatus status
) {
}
