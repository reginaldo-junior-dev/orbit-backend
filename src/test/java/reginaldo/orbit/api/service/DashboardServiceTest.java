package reginaldo.orbit.api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reginaldo.orbit.api.dto.dashboard.DashboardResponse;
import reginaldo.orbit.api.entity.User;
import reginaldo.orbit.api.enums.GoalStatus;
import reginaldo.orbit.api.enums.ProjectStatus;
import reginaldo.orbit.api.enums.TaskStatus;
import reginaldo.orbit.api.exception.UserNotFoundException;
import reginaldo.orbit.api.repository.GoalRepository;
import reginaldo.orbit.api.repository.ProjectRepository;
import reginaldo.orbit.api.repository.TaskRepository;
import reginaldo.orbit.api.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DashboardServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private GoalRepository goalRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void deveAgregarContagensDoUsuario() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        when(userRepository.findByEmail("user@orbit.com")).thenReturn(Optional.of(user));

        when(taskRepository.countTasksByStatus(userId)).thenReturn(List.of(
                new Object[]{TaskStatus.TODO, 3L},
                new Object[]{TaskStatus.DONE, 2L}
        ));
        when(projectRepository.countProjectsByStatus(userId))
                .thenReturn(Collections.singletonList(new Object[]{ProjectStatus.ACTIVE, 1L}));
        when(goalRepository.countGoalsByStatus(userId)).thenReturn(List.<Object[]>of());

        DashboardResponse dashboard = dashboardService.getDashboard("user@orbit.com");

        assertEquals(5L, dashboard.tasks().total());
        assertEquals(3L, dashboard.tasks().todo());
        assertEquals(0L, dashboard.tasks().inProgress());
        assertEquals(2L, dashboard.tasks().done());
        assertEquals(1L, dashboard.projects().total());
        assertEquals(1L, dashboard.projects().active());
        assertEquals(0L, dashboard.projects().completed());
        assertEquals(0L, dashboard.goals().total());
        assertEquals(0L, dashboard.goals().active());
    }

    @Test
    void deveLancar404ComUsuarioInexistente() {
        when(userRepository.findByEmail("nao-existe@orbit.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> dashboardService.getDashboard("nao-existe@orbit.com"));
    }
}
