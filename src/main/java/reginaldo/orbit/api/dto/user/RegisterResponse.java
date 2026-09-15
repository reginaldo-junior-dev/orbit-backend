package reginaldo.orbit.api.dto.user;

import java.util.UUID;

public record RegisterResponse (
        UUID id,
        String name,
        String email
) {
}
