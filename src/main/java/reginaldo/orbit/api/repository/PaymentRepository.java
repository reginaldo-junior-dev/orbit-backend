package reginaldo.orbit.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import reginaldo.orbit.api.entity.Payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByIdAndUserId(UUID id, UUID userId);
}
