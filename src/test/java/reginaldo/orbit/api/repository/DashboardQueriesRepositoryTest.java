package reginaldo.orbit.api.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import reginaldo.orbit.api.entity.Goal;
import reginaldo.orbit.api.entity.Project;
import reginaldo.orbit.api.entity.Task;
import reginaldo.orbit.api.entity.User;
import reginaldo.orbit.api.enums.GoalStatus;
import reginaldo.orbit.api.enums.ProjectStatus;
import reginaldo.orbit.api.enums.Role;
import reginaldo.orbit.api.enums.TaskPriority;
import reginaldo.orbit.api.enums.TaskStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class DashboardQueriesRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private GoalRepository goalRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("Repo Test");
        user.setEmail("repo-" + UUID.randomUUID() + "@orbit.com");
        user.setPassword("senha-qualquer");
        user.setRole(Role.USER);
        userRepository.save(user);
    }

    @Test
    void deveContarTasksPorStatusDoUsuario() {
        criarTask(TaskStatus.TODO);
        criarTask(TaskStatus.TODO);
        criarTask(TaskStatus.DONE);

        Map<TaskStatus, Long> counts = toMap(taskRepository.countTasksByStatus(user.getId()));

        assertEquals(2L, counts.getOrDefault(TaskStatus.TODO, 0L));
        assertEquals(1L, counts.getOrDefault(TaskStatus.DONE, 0L));
        assertEquals(0L, counts.getOrDefault(TaskStatus.IN_PROGRESS, 0L));
    }

    @Test
    void deveContarProjectsPorStatusDoUsuario() {
        criarProject(ProjectStatus.ACTIVE);
        criarProject(ProjectStatus.ARCHIVED);

        Map<ProjectStatus, Long> counts = toMap(projectRepository.countProjectsByStatus(user.getId()));

        assertEquals(1L, counts.getOrDefault(ProjectStatus.ACTIVE, 0L));
        assertEquals(1L, counts.getOrDefault(ProjectStatus.ARCHIVED, 0L));
        assertEquals(0L, counts.getOrDefault(ProjectStatus.COMPLETED, 0L));
    }

    @Test
    void deveContarGoalsPorStatusDoUsuario() {
        criarGoal(GoalStatus.ACTIVE);
        criarGoal(GoalStatus.ACTIVE);
        criarGoal(GoalStatus.COMPLETED);
        criarGoal(GoalStatus.ARCHIVED);

        Map<GoalStatus, Long> counts = toMap(goalRepository.countGoalsByStatus(user.getId()));

        assertEquals(2L, counts.getOrDefault(GoalStatus.ACTIVE, 0L));
        assertEquals(1L, counts.getOrDefault(GoalStatus.COMPLETED, 0L));
        assertEquals(1L, counts.getOrDefault(GoalStatus.ARCHIVED, 0L));
    }

    private void criarTask(TaskStatus status) {
        Task task = new Task();
        task.setTitle("Tarefa repo");
        task.setStatus(status);
        task.setPriority(TaskPriority.LOW);
        task.setCreatedAt(LocalDateTime.now());
        task.setUser(user);
        taskRepository.save(task);
    }

    private void criarProject(ProjectStatus status) {
        Project project = new Project();
        project.setName("Projeto repo");
        project.setStatus(status);
        project.setCreatedAt(LocalDateTime.now());
        project.setUser(user);
        projectRepository.save(project);
    }

    private void criarGoal(GoalStatus status) {
        Goal goal = new Goal();
        goal.setTitle("Goal repo");
        goal.setStatus(status);
        goal.setCreatedAt(LocalDateTime.now());
        goal.setUser(user);
        goalRepository.save(goal);
    }

    private <T> Map<T, Long> toMap(List<Object[]> rows) {
        Map<T, Long> counts = new HashMap<>();
        for (Object[] row : rows) {
            @SuppressWarnings("unchecked")
            T status = (T) row[0];
            counts.put(status, (Long) row[1]);
        }
        return counts;
    }
}
