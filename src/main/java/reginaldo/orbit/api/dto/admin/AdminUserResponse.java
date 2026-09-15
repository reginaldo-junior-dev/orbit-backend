package reginaldo.orbit.api.dto.admin;

import reginaldo.orbit.api.enums.Role;

import java.util.UUID;

public record AdminUserResponse(
        UUID id,
        String name,
        String email,
        Role role
) {
}
