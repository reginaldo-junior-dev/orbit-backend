package reginaldo.orbit.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reginaldo.orbit.api.doc.TaskApi;
import reginaldo.orbit.api.dto.task.TaskRequest;
import reginaldo.orbit.api.dto.task.TaskResponse;
import reginaldo.orbit.api.service.TaskService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("tasks")
@RequiredArgsConstructor
public class TaskController implements TaskApi {
    private final TaskService taskService;

    @PostMapping
    @Override
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request,
                                               Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.create(authentication.getName(), request));
    }

    @GetMapping
    @Override
    public ResponseEntity<List<TaskResponse>> list(Authentication authentication) {
        return ResponseEntity.ok(taskService.list(authentication.getName()));
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<TaskResponse> getById(@PathVariable UUID id,
                                                Authentication authentication) {
        return ResponseEntity.ok(taskService.getById(authentication.getName(), id));
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<TaskResponse> update(@PathVariable UUID id,
                                               @Valid @RequestBody TaskRequest request,
                                               Authentication authentication) {
        return ResponseEntity.ok(taskService.update(authentication.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                       Authentication authentication) {
        taskService.delete(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
