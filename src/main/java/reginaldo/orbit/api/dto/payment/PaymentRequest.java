package reginaldo.orbit.api.dto.payment;

import jakarta.validation.constraints.NotNull;
import reginaldo.orbit.api.enums.BillingCycle;
import reginaldo.orbit.api.enums.PlanType;

public record PaymentRequest(
        @NotNull(message = "Plan is required")
        PlanType plan,
        @NotNull(message = "Billing cycle is required")
        BillingCycle billingCycle
) {
}
