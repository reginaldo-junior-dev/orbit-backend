package reginaldo.orbit.api.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reginaldo.orbit.api.entity.User;
import reginaldo.orbit.api.enums.Role;
import reginaldo.orbit.api.repository.UserRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class DemoAccountsSeeder implements ApplicationRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${DEMO_ADMIN_EMAIL:admin@orbit.com}")
    private String adminEmail;

    @Value("${DEMO_ADMIN_PASSWORD:}")
    private String adminPassword;

    @Value("${DEMO_USER_EMAIL:user@orbit.com}")
    private String userEmail;

    @Value("${DEMO_USER_PASSWORD:}")
    private String userPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seed(adminEmail, "Administrator", adminPassword, Role.ADMIN);
        seed(userEmail, "Lucas", userPassword, Role.USER);
    }

    private void seed(String email, String name, String rawPassword, Role role) {
        if (rawPassword == null || rawPassword.isBlank()) {
            log.info("Skipping demo account seed for {} (no password configured)", email);
            return;
        }

        User user = userRepository.findByEmail(email).orElseGet(User::new);
        user.setName(name);
        user.setEmail(email);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
        log.info("Demo account ready: {}", email);
    }
}
