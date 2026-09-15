package reginaldo.orbit.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reginaldo.orbit.api.dto.project.ProjectRequest;
import reginaldo.orbit.api.dto.project.ProjectResponse;
import reginaldo.orbit.api.entity.Project;
import reginaldo.orbit.api.entity.User;
import reginaldo.orbit.api.enums.ProjectStatus;
import reginaldo.orbit.api.exception.ProjectHasTasksException;
import reginaldo.orbit.api.exception.ProjectNotFoundException;
import reginaldo.orbit.api.repository.ProjectRepository;
import reginaldo.orbit.api.repository.TaskRepository;
import reginaldo.orbit.api.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private ProjectService projectService;

    private User user;
    private UUID userId;
    private UUID projectId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setEmail("user@orbit.com");
    }

    @Test
    void deveCriarProjetoAtribuindoUsuarioAutenticado() {
        when(userRepository.findByEmail("user@orbit.com")).thenReturn(Optional.of(user));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectRequest request = new ProjectRequest("Projeto", "Desc", ProjectStatus.ACTIVE);
        ProjectResponse response = projectService.create("user@orbit.com", request);

        assertEquals("Projeto", response.name());
        assertEquals(ProjectStatus.ACTIVE, response.status());
        assertNotNull(response.createdAt());
    }

    @Test
    void deveLancar404AoBuscarProjetoDeOutroUsuario() {
        when(userRepository.findByEmail("user@orbit.com")).thenReturn(Optional.of(user));
        when(projectRepository.findByIdAndUserId(projectId, userId)).thenReturn(Optional.empty());

        assertThrows(ProjectNotFoundException.class, () -> projectService.getById("user@orbit.com", projectId));
    }

    @Test
    void deveLancar409AoExcluirProjetoComTarefas() {
        Project project = new Project();
        project.setId(projectId);
        when(userRepository.findByEmail("user@orbit.com")).thenReturn(Optional.of(user));
        when(projectRepository.findByIdAndUserId(projectId, userId)).thenReturn(Optional.of(project));
        when(taskRepository.existsByProjectId(projectId)).thenReturn(true);

        assertThrows(ProjectHasTasksException.class, () -> projectService.delete("user@orbit.com", projectId));
        verify(projectRepository, never()).delete(any(Project.class));
    }

    @Test
    void deveExcluirProjetoSemTarefas() {
        Project project = new Project();
        project.setId(projectId);
        when(userRepository.findByEmail("user@orbit.com")).thenReturn(Optional.of(user));
        when(projectRepository.findByIdAndUserId(projectId, userId)).thenReturn(Optional.of(project));
        when(taskRepository.existsByProjectId(projectId)).thenReturn(false);

        projectService.delete("user@orbit.com", projectId);

        verify(projectRepository).delete(project);
    }
}
