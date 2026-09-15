package reginaldo.orbit.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reginaldo.orbit.api.dto.dashboard.DashboardResponse;
import reginaldo.orbit.api.dto.dashboard.DashboardResponse.GoalSummary;
import reginaldo.orbit.api.dto.dashboard.DashboardResponse.ProjectSummary;
import reginaldo.orbit.api.dto.dashboard.DashboardResponse.TaskSummary;
import reginaldo.orbit.api.entity.User;
import reginaldo.orbit.api.enums.GoalStatus;
import reginaldo.orbit.api.enums.ProjectStatus;
import reginaldo.orbit.api.enums.TaskStatus;
import reginaldo.orbit.api.exception.UserNotFoundException;
import reginaldo.orbit.api.repository.GoalRepository;
import reginaldo.orbit.api.repository.ProjectRepository;
import reginaldo.orbit.api.repository.TaskRepository;
import reginaldo.orbit.api.repository.UserRepository;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final GoalRepository goalRepository;

    public DashboardResponse getDashboard(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        UUID userId = user.getId();

        return new DashboardResponse(
                buildTaskSummary(userId),
                buildProjectSummary(userId),
                buildGoalSummary(userId)
        );
    }

    private TaskSummary buildTaskSummary(UUID userId) {
        Map<TaskStatus, Long> counts = new EnumMap<>(TaskStatus.class);
        for (Object[] row : taskRepository.countTasksByStatus(userId)) {
            counts.put((TaskStatus) row[0], (Long) row[1]);
        }
        long total = counts.values().stream().mapToLong(Long::longValue).sum();
        return new TaskSummary(
                total,
                counts.getOrDefault(TaskStatus.TODO, 0L),
                counts.getOrDefault(TaskStatus.IN_PROGRESS, 0L),
                counts.getOrDefault(TaskStatus.DONE, 0L)
        );
    }

    private ProjectSummary buildProjectSummary(UUID userId) {
        Map<ProjectStatus, Long> counts = new EnumMap<>(ProjectStatus.class);
        for (Object[] row : projectRepository.countProjectsByStatus(userId)) {
            counts.put((ProjectStatus) row[0], (Long) row[1]);
        }
        long total = counts.values().stream().mapToLong(Long::longValue).sum();
        return new ProjectSummary(
                total,
                counts.getOrDefault(ProjectStatus.ACTIVE, 0L),
                counts.getOrDefault(ProjectStatus.COMPLETED, 0L),
                counts.getOrDefault(ProjectStatus.ARCHIVED, 0L)
        );
    }

    private GoalSummary buildGoalSummary(UUID userId) {
        Map<GoalStatus, Long> counts = new EnumMap<>(GoalStatus.class);
        for (Object[] row : goalRepository.countGoalsByStatus(userId)) {
            counts.put((GoalStatus) row[0], (Long) row[1]);
        }
        long total = counts.values().stream().mapToLong(Long::longValue).sum();
        return new GoalSummary(
                total,
                counts.getOrDefault(GoalStatus.ACTIVE, 0L),
                counts.getOrDefault(GoalStatus.COMPLETED, 0L),
                counts.getOrDefault(GoalStatus.ARCHIVED, 0L)
        );
    }
}
