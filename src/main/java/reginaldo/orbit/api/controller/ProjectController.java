package reginaldo.orbit.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reginaldo.orbit.api.doc.ProjectApi;
import reginaldo.orbit.api.dto.project.ProjectRequest;
import reginaldo.orbit.api.dto.project.ProjectResponse;
import reginaldo.orbit.api.service.ProjectService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("projects")
@RequiredArgsConstructor
public class ProjectController implements ProjectApi {
    private final ProjectService projectService;

    @PostMapping
    @Override
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectRequest request,
                                                  Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.create(authentication.getName(), request));
    }

    @GetMapping
    @Override
    public ResponseEntity<List<ProjectResponse>> list(Authentication authentication) {
        return ResponseEntity.ok(projectService.list(authentication.getName()));
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<ProjectResponse> getById(@PathVariable UUID id,
                                                   Authentication authentication) {
        return ResponseEntity.ok(projectService.getById(authentication.getName(), id));
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<ProjectResponse> update(@PathVariable UUID id,
                                                  @Valid @RequestBody ProjectRequest request,
                                                  Authentication authentication) {
        return ResponseEntity.ok(projectService.update(authentication.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                       Authentication authentication) {
        projectService.delete(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
