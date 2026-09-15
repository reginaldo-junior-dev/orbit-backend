package reginaldo.orbit.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reginaldo.orbit.api.dto.task.TaskRequest;
import reginaldo.orbit.api.dto.task.TaskResponse;
import reginaldo.orbit.api.entity.Task;
import reginaldo.orbit.api.entity.User;
import reginaldo.orbit.api.enums.TaskPriority;
import reginaldo.orbit.api.enums.TaskStatus;
import reginaldo.orbit.api.exception.ProjectNotFoundException;
import reginaldo.orbit.api.exception.TaskNotFoundException;
import reginaldo.orbit.api.exception.UserNotFoundException;
import reginaldo.orbit.api.repository.ProjectRepository;
import reginaldo.orbit.api.repository.TaskRepository;
import reginaldo.orbit.api.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private TaskService taskService;

    private User user;
    private UUID userId;
    private UUID taskId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        taskId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setEmail("user@orbit.com");
    }

    @Test
    void deveCriarTarefaAtribuindoUsuarioAutenticado() {
        when(userRepository.findByEmail("user@orbit.com")).thenReturn(Optional.of(user));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskRequest request = new TaskRequest("Tarefa", "Desc", TaskStatus.TODO, TaskPriority.HIGH, null, null);
        TaskResponse response = taskService.create("user@orbit.com", request);

        assertEquals("Tarefa", response.title());
        assertEquals(TaskStatus.TODO, response.status());
        assertNotNull(response.createdAt());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void deveLancar404AoAcessarComUsuarioInexistente() {
        when(userRepository.findByEmail("nao-existe@orbit.com")).thenReturn(Optional.empty());

        TaskRequest request = new TaskRequest("Tarefa", null, TaskStatus.TODO, TaskPriority.LOW, null, null);
        assertThrows(UserNotFoundException.class, () -> taskService.create("nao-existe@orbit.com", request));
    }

    @Test
    void deveLancar404AoTentarAssociarProjetoDeOutroUsuario() {
        when(userRepository.findByEmail("user@orbit.com")).thenReturn(Optional.of(user));
        when(projectRepository.findByIdAndUserId(any(UUID.class), eq(userId))).thenReturn(Optional.empty());

        TaskRequest request = new TaskRequest("Tarefa", null, TaskStatus.TODO, TaskPriority.LOW, null, UUID.randomUUID());
        assertThrows(ProjectNotFoundException.class, () -> taskService.create("user@orbit.com", request));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void deveLancar404AoBuscarTarefaDeOutroUsuario() {
        when(userRepository.findByEmail("user@orbit.com")).thenReturn(Optional.of(user));
        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.getById("user@orbit.com", taskId));
    }

    @Test
    void deveListarSomenteTarefasDoUsuario() {
        when(userRepository.findByEmail("user@orbit.com")).thenReturn(Optional.of(user));

        Task task = new Task();
        task.setId(taskId);
        task.setTitle("T1");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.LOW);
        when(taskRepository.findByUserId(userId)).thenReturn(List.of(task));

        List<TaskResponse> responses = taskService.list("user@orbit.com");

        assertEquals(1, responses.size());
        assertEquals("T1", responses.get(0).title());
        verify(taskRepository).findByUserId(userId);
    }

    @Test
    void deveLancar404AoExcluirTarefaDeOutroUsuario() {
        when(userRepository.findByEmail("user@orbit.com")).thenReturn(Optional.of(user));
        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.delete("user@orbit.com", taskId));
        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    void deveExcluirTarefaPropria() {
        Task task = new Task();
        task.setId(taskId);
        when(userRepository.findByEmail("user@orbit.com")).thenReturn(Optional.of(user));
        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.of(task));

        taskService.delete("user@orbit.com", taskId);

        verify(taskRepository).delete(task);
    }
}
