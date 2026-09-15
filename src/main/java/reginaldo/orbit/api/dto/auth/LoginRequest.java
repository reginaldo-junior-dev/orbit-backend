package reginaldo.orbit.api.dto.auth;

public record LoginRequest(
        String email,
        String password
) {
}
