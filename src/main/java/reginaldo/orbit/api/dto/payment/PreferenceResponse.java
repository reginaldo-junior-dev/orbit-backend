package reginaldo.orbit.api.dto.payment;

import java.util.UUID;

public record PreferenceResponse(
        UUID paymentId,
        String checkoutUrl
) {
}
