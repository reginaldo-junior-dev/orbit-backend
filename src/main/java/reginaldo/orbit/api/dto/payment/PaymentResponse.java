package reginaldo.orbit.api.dto.payment;

import reginaldo.orbit.api.enums.BillingCycle;
import reginaldo.orbit.api.enums.PaymentStatus;
import reginaldo.orbit.api.enums.PlanType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        PlanType plan,
        BillingCycle billingCycle,
        BigDecimal amount,
        PaymentStatus status,
        LocalDateTime createdAt
) {
}
