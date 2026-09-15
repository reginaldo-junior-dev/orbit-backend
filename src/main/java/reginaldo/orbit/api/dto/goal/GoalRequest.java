package reginaldo.orbit.api.dto.goal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import reginaldo.orbit.api.enums.GoalStatus;

import java.time.LocalDate;

public record GoalRequest(
        @NotBlank(message = "Title is required")
        String title,
        String description,
        @NotNull(message = "Status is required")
        GoalStatus status,
        LocalDate targetDate
) {
}
