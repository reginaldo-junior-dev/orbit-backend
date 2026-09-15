package reginaldo.orbit.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reginaldo.orbit.api.dto.task.TaskRequest;
import reginaldo.orbit.api.dto.task.TaskResponse;
import reginaldo.orbit.api.entity.Project;
import reginaldo.orbit.api.entity.Task;
import reginaldo.orbit.api.entity.User;
import reginaldo.orbit.api.exception.ProjectNotFoundException;
import reginaldo.orbit.api.exception.TaskNotFoundException;
import reginaldo.orbit.api.exception.UserNotFoundException;
import reginaldo.orbit.api.repository.ProjectRepository;
import reginaldo.orbit.api.repository.TaskRepository;
import reginaldo.orbit.api.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public TaskResponse create(String email, TaskRequest request) {
        User user = getUserByEmail(email);

        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setPriority(request.priority());
        task.setDueDate(request.dueDate());
        task.setCreatedAt(LocalDateTime.now());
        task.setUser(user);
        task.setProject(resolveProject(user, request.projectId()));

        return toResponse(taskRepository.save(task));
    }

    public List<TaskResponse> list(String email) {
        User user = getUserByEmail(email);
        return taskRepository.findByUserId(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public TaskResponse getById(String email, UUID id) {
        User user = getUserByEmail(email);
        return toResponse(findOwnedTask(user, id));
    }

    @Transactional
    public TaskResponse update(String email, UUID id, TaskRequest request) {
        User user = getUserByEmail(email);
        Task task = findOwnedTask(user, id);

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setPriority(request.priority());
        task.setDueDate(request.dueDate());
        task.setProject(resolveProject(user, request.projectId()));

        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public void delete(String email, UUID id) {
        User user = getUserByEmail(email);
        Task task = findOwnedTask(user, id);
        taskRepository.delete(task);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private Task findOwnedTask(User user, UUID id) {
        return taskRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));
    }

    private Project resolveProject(User user, UUID projectId) {
        if (projectId == null) {
            return null;
        }
        return projectRepository.findByIdAndUserId(projectId, user.getId())
                .orElseThrow(() -> new ProjectNotFoundException("Project not found"));
    }

    private TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getCreatedAt(),
                task.getProject() != null ? task.getProject().getId() : null
        );
    }
}
