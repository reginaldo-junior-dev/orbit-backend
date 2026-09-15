package reginaldo.orbit.api.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import reginaldo.orbit.api.enums.TaskPriority;
import reginaldo.orbit.api.enums.TaskStatus;

import java.time.LocalDate;
import java.util.UUID;

public record TaskRequest(
        @NotBlank(message = "Title is required")
        String title,
        String description,
        @NotNull(message = "Status is required")
        TaskStatus status,
        @NotNull(message = "Priority is required")
        TaskPriority priority,
        LocalDate dueDate,
        UUID projectId
) {
}
