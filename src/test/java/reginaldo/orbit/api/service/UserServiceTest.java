package reginaldo.orbit.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import reginaldo.orbit.api.dto.user.ChangePasswordRequest;
import reginaldo.orbit.api.dto.user.UpdateUserRequest;
import reginaldo.orbit.api.dto.user.UserResponse;
import reginaldo.orbit.api.entity.User;
import reginaldo.orbit.api.enums.Role;
import reginaldo.orbit.api.exception.EmailAlreadyExists;
import reginaldo.orbit.api.exception.InvalidCurrentPasswordException;
import reginaldo.orbit.api.repository.GoalRepository;
import reginaldo.orbit.api.repository.ProjectRepository;
import reginaldo.orbit.api.repository.TaskRepository;
import reginaldo.orbit.api.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private GoalRepository goalRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setName("Lucas");
        user.setEmail("user@orbit.com");
        user.setPassword("hash-atual");
        user.setRole(Role.USER);
    }

    @Test
    void deveLancar409AoAtualizarEmailJaEmUsoPorOutroUsuario() {
        when(userRepository.findByEmail("user@orbit.com")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("outro@orbit.com")).thenReturn(Optional.of(new User()));

        UpdateUserRequest request = new UpdateUserRequest("Lucas", "outro@orbit.com");
        assertThrows(EmailAlreadyExists.class, () -> userService.updateMe("user@orbit.com", request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deveAtualizarNomeEEmailSemConflito() {
        when(userRepository.findByEmail("user@orbit.com")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("novo@orbit.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateUserRequest request = new UpdateUserRequest("Novo Nome", "novo@orbit.com");
        UserResponse response = userService.updateMe("user@orbit.com", request);

        assertEquals("Novo Nome", response.name());
        assertEquals("novo@orbit.com", response.email());
        assertEquals(Role.USER, response.role());
    }

    @Test
    void deveLancar400ComSenhaAtualIncorreta() {
        when(userRepository.findByEmail("user@orbit.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senha-errada", "hash-atual")).thenReturn(false);

        ChangePasswordRequest request = new ChangePasswordRequest("senha-errada", "novaSenha123");
        assertThrows(InvalidCurrentPasswordException.class, () -> userService.changePassword("user@orbit.com", request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deveCodificarEGravarNovaSenha() {
        when(userRepository.findByEmail("user@orbit.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "hash-atual")).thenReturn(true);
        when(passwordEncoder.encode("novaSenha123")).thenReturn("novo-hash");

        ChangePasswordRequest request = new ChangePasswordRequest("123456", "novaSenha123");
        userService.changePassword("user@orbit.com", request);

        assertEquals("novo-hash", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void deveExcluirContaRemovendoTarefasProjetosEGoalsAntesDoUsuario() {
        when(userRepository.findByEmail("user@orbit.com")).thenReturn(Optional.of(user));

        userService.deleteMe("user@orbit.com");

        InOrder inOrder = inOrder(taskRepository, projectRepository, goalRepository, userRepository);
        inOrder.verify(taskRepository).deleteByUserId(userId);
        inOrder.verify(projectRepository).deleteByUserId(userId);
        inOrder.verify(goalRepository).deleteByUserId(userId);
        inOrder.verify(userRepository).delete(user);
    }
}
