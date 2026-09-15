package reginaldo.orbit.api.controller;

import com.mercadopago.exceptions.MPInvalidWebhookSignatureException;
import com.mercadopago.webhook.WebhookSignatureValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reginaldo.orbit.api.doc.PaymentApi;
import reginaldo.orbit.api.dto.payment.PaymentRequest;
import reginaldo.orbit.api.dto.payment.PaymentResponse;
import reginaldo.orbit.api.dto.payment.PreferenceResponse;
import reginaldo.orbit.api.service.PaymentService;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("payments")
@RequiredArgsConstructor
public class PaymentController implements PaymentApi {
    private final PaymentService paymentService;

    @Value("${MP_WEBHOOK_SECRET:}")
    private String webhookSecret;

    @PostMapping("/preference")
    @Override
    public ResponseEntity<PreferenceResponse> createPreference(@Valid @RequestBody PaymentRequest request,
                                                                 Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createPreference(authentication.getName(), request));
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<PaymentResponse> getById(@PathVariable UUID id, Authentication authentication) {
        return ResponseEntity.ok(paymentService.getById(authentication.getName(), id));
    }

    @PostMapping("/webhook")
    @Override
    public ResponseEntity<Void> webhook(@RequestParam(value = "type", required = false) String type,
                                        @RequestParam(value = "data.id", required = false) String dataId,
                                        @RequestHeader(value = "x-signature", required = false) String xSignature,
                                        @RequestHeader(value = "x-request-id", required = false) String xRequestId) {
        if (dataId == null || !"payment".equals(type)) {
            return ResponseEntity.ok().build();
        }

        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.error("MP_WEBHOOK_SECRET is not configured; rejecting webhook notification");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            WebhookSignatureValidator.validate(xSignature, xRequestId, dataId, webhookSecret);
        } catch (MPInvalidWebhookSignatureException e) {
            log.warn("Invalid Mercado Pago webhook signature for payment {}", dataId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        paymentService.processWebhook(dataId);
        return ResponseEntity.ok().build();
    }
}
