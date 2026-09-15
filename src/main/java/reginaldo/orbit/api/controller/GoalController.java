package reginaldo.orbit.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reginaldo.orbit.api.doc.GoalApi;
import reginaldo.orbit.api.dto.goal.GoalRequest;
import reginaldo.orbit.api.dto.goal.GoalResponse;
import reginaldo.orbit.api.service.GoalService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("goals")
@RequiredArgsConstructor
public class GoalController implements GoalApi {
    private final GoalService goalService;

    @PostMapping
    @Override
    public ResponseEntity<GoalResponse> create(@Valid @RequestBody GoalRequest request,
                                               Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(goalService.create(authentication.getName(), request));
    }

    @GetMapping
    @Override
    public ResponseEntity<List<GoalResponse>> list(Authentication authentication) {
        return ResponseEntity.ok(goalService.list(authentication.getName()));
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<GoalResponse> getById(@PathVariable UUID id,
                                                Authentication authentication) {
        return ResponseEntity.ok(goalService.getById(authentication.getName(), id));
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<GoalResponse> update(@PathVariable UUID id,
                                               @Valid @RequestBody GoalRequest request,
                                               Authentication authentication) {
        return ResponseEntity.ok(goalService.update(authentication.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                       Authentication authentication) {
        goalService.delete(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
