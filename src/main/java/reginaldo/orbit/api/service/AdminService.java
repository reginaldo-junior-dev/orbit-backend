package reginaldo.orbit.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reginaldo.orbit.api.dto.admin.AdminUpdateUserRequest;
import reginaldo.orbit.api.dto.admin.AdminUserResponse;
import reginaldo.orbit.api.entity.User;
import reginaldo.orbit.api.exception.CannotModifySelfException;
import reginaldo.orbit.api.exception.EmailAlreadyExists;
import reginaldo.orbit.api.exception.UserNotFoundException;
import reginaldo.orbit.api.repository.GoalRepository;
import reginaldo.orbit.api.repository.PaymentRepository;
import reginaldo.orbit.api.repository.ProjectRepository;
import reginaldo.orbit.api.repository.TaskRepository;
import reginaldo.orbit.api.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final GoalRepository goalRepository;
    private final PaymentRepository paymentRepository;

    public List<AdminUserResponse> listUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public AdminUserResponse getUserById(UUID id) {
        return toResponse(getUser(id));
    }

    @Transactional
    public AdminUserResponse updateUser(UUID id, String currentAdminEmail, AdminUpdateUserRequest request) {
        User user = getUser(id);

        if (user.getEmail().equalsIgnoreCase(currentAdminEmail)) {
            throw new CannotModifySelfException("Use your own profile settings to edit your account");
        }

        String newEmail = request.email();
        if (!user.getEmail().equalsIgnoreCase(newEmail) && userRepository.findByEmail(newEmail).isPresent()) {
            throw new EmailAlreadyExists("Email already exists");
        }

        user.setName(request.name());
        user.setEmail(newEmail);
        user.setRole(request.role());
        userRepository.save(user);

        return toResponse(user);
    }

    @Transactional
    public void deleteUser(UUID id, String currentAdminEmail) {
        User user = getUser(id);

        if (user.getEmail().equalsIgnoreCase(currentAdminEmail)) {
            throw new CannotModifySelfException("You cannot delete your own account here");
        }

        taskRepository.deleteByUserId(user.getId());
        projectRepository.deleteByUserId(user.getId());
        goalRepository.deleteByUserId(user.getId());
        paymentRepository.deleteByUserId(user.getId());
        userRepository.delete(user);
    }

    private User getUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private AdminUserResponse toResponse(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getPlan(),
                user.getPlanExpiresAt()
        );
    }
}
