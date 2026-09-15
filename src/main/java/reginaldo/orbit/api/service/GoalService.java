package reginaldo.orbit.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reginaldo.orbit.api.dto.goal.GoalRequest;
import reginaldo.orbit.api.dto.goal.GoalResponse;
import reginaldo.orbit.api.entity.Goal;
import reginaldo.orbit.api.entity.User;
import reginaldo.orbit.api.exception.GoalNotFoundException;
import reginaldo.orbit.api.exception.UserNotFoundException;
import reginaldo.orbit.api.repository.GoalRepository;
import reginaldo.orbit.api.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoalService {
    private final GoalRepository goalRepository;
    private final UserRepository userRepository;

    public GoalResponse create(String email, GoalRequest request) {
        User user = getUserByEmail(email);

        Goal goal = new Goal();
        goal.setTitle(request.title());
        goal.setDescription(request.description());
        goal.setStatus(request.status());
        goal.setTargetDate(request.targetDate());
        goal.setCreatedAt(LocalDateTime.now());
        goal.setUser(user);

        return toResponse(goalRepository.save(goal));
    }

    public List<GoalResponse> list(String email) {
        User user = getUserByEmail(email);
        return goalRepository.findByUserId(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public GoalResponse getById(String email, UUID id) {
        User user = getUserByEmail(email);
        return toResponse(findOwnedGoal(user, id));
    }

    @Transactional
    public GoalResponse update(String email, UUID id, GoalRequest request) {
        User user = getUserByEmail(email);
        Goal goal = findOwnedGoal(user, id);

        goal.setTitle(request.title());
        goal.setDescription(request.description());
        goal.setStatus(request.status());
        goal.setTargetDate(request.targetDate());

        return toResponse(goalRepository.save(goal));
    }

    @Transactional
    public void delete(String email, UUID id) {
        User user = getUserByEmail(email);
        Goal goal = findOwnedGoal(user, id);
        goalRepository.delete(goal);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private Goal findOwnedGoal(User user, UUID id) {
        return goalRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new GoalNotFoundException("Goal not found"));
    }

    private GoalResponse toResponse(Goal goal) {
        return new GoalResponse(
                goal.getId(),
                goal.getTitle(),
                goal.getDescription(),
                goal.getStatus(),
                goal.getTargetDate(),
                goal.getCreatedAt()
        );
    }
}
