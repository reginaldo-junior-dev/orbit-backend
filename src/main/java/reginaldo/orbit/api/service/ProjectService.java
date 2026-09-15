package reginaldo.orbit.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reginaldo.orbit.api.dto.project.ProjectRequest;
import reginaldo.orbit.api.dto.project.ProjectResponse;
import reginaldo.orbit.api.entity.Project;
import reginaldo.orbit.api.entity.User;
import reginaldo.orbit.api.exception.ProjectHasTasksException;
import reginaldo.orbit.api.exception.ProjectNotFoundException;
import reginaldo.orbit.api.exception.UserNotFoundException;
import reginaldo.orbit.api.repository.ProjectRepository;
import reginaldo.orbit.api.repository.TaskRepository;
import reginaldo.orbit.api.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public ProjectResponse create(String email, ProjectRequest request) {
        User user = getUserByEmail(email);

        Project project = new Project();
        project.setName(request.name());
        project.setDescription(request.description());
        project.setStatus(request.status());
        project.setCreatedAt(LocalDateTime.now());
        project.setUser(user);

        return toResponse(projectRepository.save(project));
    }

    public List<ProjectResponse> list(String email) {
        User user = getUserByEmail(email);
        return projectRepository.findByUserId(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ProjectResponse getById(String email, UUID id) {
        User user = getUserByEmail(email);
        return toResponse(findOwnedProject(user, id));
    }

    @Transactional
    public ProjectResponse update(String email, UUID id, ProjectRequest request) {
        User user = getUserByEmail(email);
        Project project = findOwnedProject(user, id);

        project.setName(request.name());
        project.setDescription(request.description());
        project.setStatus(request.status());

        return toResponse(projectRepository.save(project));
    }

    @Transactional
    public void delete(String email, UUID id) {
        User user = getUserByEmail(email);
        Project project = findOwnedProject(user, id);

        if (taskRepository.existsByProjectId(id)) {
            throw new ProjectHasTasksException("Cannot delete a project that has tasks");
        }

        projectRepository.delete(project);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private Project findOwnedProject(User user, UUID id) {
        return projectRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ProjectNotFoundException("Project not found"));
    }

    private ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getCreatedAt()
        );
    }
}
