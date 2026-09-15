package reginaldo.orbit.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reginaldo.orbit.api.dto.admin.AdminUserResponse;
import reginaldo.orbit.api.entity.User;
import reginaldo.orbit.api.exception.UserNotFoundException;
import reginaldo.orbit.api.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;

    public List<AdminUserResponse> listUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public AdminUserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return toResponse(user);
    }

    private AdminUserResponse toResponse(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}
