package reginaldo.orbit.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reginaldo.orbit.api.dto.user.RegisterRequest;
import reginaldo.orbit.api.dto.user.RegisterResponse;
import reginaldo.orbit.api.entity.User;
import reginaldo.orbit.api.enums.Role;
import reginaldo.orbit.api.exception.EmailAlreadyExists;
import reginaldo.orbit.api.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class RegisterService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterResponse register (RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.email()).isPresent()) {
            throw new EmailAlreadyExists("Email already exists");
        }
            User user = new User();
            user.setName(registerRequest.name());
            user.setEmail(registerRequest.email());
            user.setRole(Role.USER);
            user.setPassword(passwordEncoder.encode(registerRequest.password()));

            User userSave = userRepository.save(user);

            RegisterResponse registerResponse = new RegisterResponse(
                    userSave.getId(),
                    userSave.getName(),
                    userSave.getEmail()
            );

            return registerResponse;
        }

    }

