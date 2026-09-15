package reginaldo.orbit.api.security;

import org.springframework.stereotype.Component;
import reginaldo.orbit.api.exception.TooManyRequestsException;

import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class LoginRateLimiter {
    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);

    private final Map<String, Deque<Instant>> attemptsByEmail = new ConcurrentHashMap<>();

    public void checkAllowed(String email) {
        Deque<Instant> attempts = attemptsByEmail.getOrDefault(normalize(email), new ConcurrentLinkedDeque<>());
        Instant cutoff = Instant.now().minus(WINDOW);
        while (!attempts.isEmpty() && attempts.peekFirst().isBefore(cutoff)) {
            attempts.pollFirst();
        }
        if (attempts.size() >= MAX_ATTEMPTS) {
            throw new TooManyRequestsException("Too many login attempts. Try again in a few minutes.");
        }
    }

    public void recordFailure(String email) {
        attemptsByEmail
                .computeIfAbsent(normalize(email), key -> new ConcurrentLinkedDeque<>())
                .addLast(Instant.now());
    }

    public void reset(String email) {
        attemptsByEmail.remove(normalize(email));
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
