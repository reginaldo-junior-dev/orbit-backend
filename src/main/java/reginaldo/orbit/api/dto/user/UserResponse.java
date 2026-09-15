package reginaldo.orbit.api.dto.user;

import reginaldo.orbit.api.enums.PlanType;
import reginaldo.orbit.api.enums.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        Role role,
        PlanType plan,
        LocalDateTime planExpiresAt
) {
}
