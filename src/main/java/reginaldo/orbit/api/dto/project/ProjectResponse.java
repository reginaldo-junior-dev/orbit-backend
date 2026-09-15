package reginaldo.orbit.api.dto.project;

import reginaldo.orbit.api.enums.ProjectStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String name,
        String description,
        ProjectStatus status,
        LocalDateTime createdAt
) {
}
