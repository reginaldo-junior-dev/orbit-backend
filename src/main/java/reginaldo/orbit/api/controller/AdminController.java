package reginaldo.orbit.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reginaldo.orbit.api.doc.AdminApi;
import reginaldo.orbit.api.dto.admin.AdminUpdateUserRequest;
import reginaldo.orbit.api.dto.admin.AdminUserResponse;
import reginaldo.orbit.api.service.AdminService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("admin")
@RequiredArgsConstructor
public class AdminController implements AdminApi {
    private final AdminService adminService;

    @GetMapping("users")
    @Override
    public ResponseEntity<List<AdminUserResponse>> listUsers() {
        return ResponseEntity.ok(adminService.listUsers());
    }

    @GetMapping("users/{id}")
    @Override
    public ResponseEntity<AdminUserResponse> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    @PutMapping("users/{id}")
    @Override
    public ResponseEntity<AdminUserResponse> updateUser(@PathVariable UUID id,
                                                         @Valid @RequestBody AdminUpdateUserRequest request,
                                                         Authentication authentication) {
        return ResponseEntity.ok(adminService.updateUser(id, authentication.getName(), request));
    }

    @DeleteMapping("users/{id}")
    @Override
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id, Authentication authentication) {
        adminService.deleteUser(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
