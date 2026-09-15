package reginaldo.orbit.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reginaldo.orbit.api.doc.UserApi;
import reginaldo.orbit.api.dto.user.ChangePasswordRequest;
import reginaldo.orbit.api.dto.user.UpdateUserRequest;
import reginaldo.orbit.api.dto.user.UserResponse;
import reginaldo.orbit.api.service.UserService;

@RestController
@RequestMapping("users")
@RequiredArgsConstructor
public class UserController implements UserApi {
    private final UserService userService;

    @GetMapping("/me")
    @Override
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        return ResponseEntity.ok(userService.me(authentication.getName()));
    }

    @PutMapping("/me")
    @Override
    public ResponseEntity<UserResponse> updateMe(@Valid @RequestBody UpdateUserRequest request,
                                                 Authentication authentication) {
        return ResponseEntity.ok(userService.updateMe(authentication.getName(), request));
    }

    @PatchMapping("/me/password")
    @Override
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                               Authentication authentication) {
        userService.changePassword(authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    @Override
    public ResponseEntity<Void> deleteMe(Authentication authentication) {
        userService.deleteMe(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
