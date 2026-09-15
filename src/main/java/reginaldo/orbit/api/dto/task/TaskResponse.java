package reginaldo.orbit.api.dto.task;

import reginaldo.orbit.api.enums.TaskPriority;
import reginaldo.orbit.api.enums.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate,
        LocalDateTime createdAt,
        UUID projectId
) {
}
