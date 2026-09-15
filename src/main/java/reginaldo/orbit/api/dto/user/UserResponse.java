package reginaldo.orbit.api.dto.user;

import reginaldo.orbit.api.enums.Role;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        Role role
) {
}
