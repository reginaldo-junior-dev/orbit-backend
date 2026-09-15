package reginaldo.orbit.api.dto.goal;

import reginaldo.orbit.api.enums.GoalStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record GoalResponse(
        UUID id,
        String title,
        String description,
        GoalStatus status,
        LocalDate targetDate,
        LocalDateTime createdAt
) {
}
