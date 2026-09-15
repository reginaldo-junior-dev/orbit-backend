package reginaldo.orbit.api.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import reginaldo.orbit.api.enums.Role;

public record AdminUpdateUserRequest(
        @NotBlank(message = "Name is required")
        String name,
        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        String email,
        @NotNull(message = "Role is required")
        Role role
) {
}
