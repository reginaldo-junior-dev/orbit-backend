package reginaldo.orbit.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reginaldo.orbit.api.dto.user.ChangePasswordRequest;
import reginaldo.orbit.api.dto.user.UpdateUserRequest;
import reginaldo.orbit.api.dto.user.UserResponse;
import reginaldo.orbit.api.entity.User;
import reginaldo.orbit.api.exception.EmailAlreadyExists;
import reginaldo.orbit.api.exception.InvalidCurrentPasswordException;
import reginaldo.orbit.api.exception.UserNotFoundException;
import reginaldo.orbit.api.repository.GoalRepository;
import reginaldo.orbit.api.repository.ProjectRepository;
import reginaldo.orbit.api.repository.PaymentRepository;
import reginaldo.orbit.api.repository.TaskRepository;
import reginaldo.orbit.api.repository.UserRepository;
import reginaldo.orbit.api.enums.PlanType;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final GoalRepository goalRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse me(String email) {
        User user = getUserByEmail(email);
        return toResponse(user);
    }

    @Transactional
    public UserResponse updateMe(String email, UpdateUserRequest request) {
        User user = getUserByEmail(email);

        String newEmail = request.email();
        if (!user.getEmail().equals(newEmail) && userRepository.findByEmail(newEmail).isPresent()) {
            throw new EmailAlreadyExists("Email already exists");
        }

        user.setName(request.name());
        user.setEmail(newEmail);
        userRepository.save(user);

        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        boolean planActive = user.getPlanExpiresAt() != null
                && user.getPlanExpiresAt().isAfter(LocalDateTime.now());

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                planActive ? user.getPlan() : PlanType.STARTER,
                planActive ? user.getPlanExpiresAt() : null
        );
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = getUserByEmail(email);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new InvalidCurrentPasswordException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void deleteMe(String email) {
        User user = getUserByEmail(email);

        taskRepository.deleteByUserId(user.getId());
        projectRepository.deleteByUserId(user.getId());
        goalRepository.deleteByUserId(user.getId());
        paymentRepository.deleteByUserId(user.getId());
        userRepository.delete(user);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}
