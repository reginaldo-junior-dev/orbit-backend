package reginaldo.orbit.api.service;

import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reginaldo.orbit.api.dto.payment.PaymentRequest;
import reginaldo.orbit.api.dto.payment.PaymentResponse;
import reginaldo.orbit.api.dto.payment.PreferenceResponse;
import reginaldo.orbit.api.entity.Payment;
import reginaldo.orbit.api.entity.User;
import reginaldo.orbit.api.enums.BillingCycle;
import reginaldo.orbit.api.enums.PaymentStatus;
import reginaldo.orbit.api.enums.PlanType;
import reginaldo.orbit.api.exception.InvalidPlanException;
import reginaldo.orbit.api.exception.PaymentGatewayException;
import reginaldo.orbit.api.exception.PaymentNotFoundException;
import reginaldo.orbit.api.exception.UserNotFoundException;
import reginaldo.orbit.api.repository.PaymentRepository;
import reginaldo.orbit.api.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Value("${FRONTEND_ORIGIN:http://localhost:5173}")
    private String frontendOrigin;

    @Value("${MP_NOTIFICATION_URL:}")
    private String notificationUrl;

    // Preço cobrado uma única vez: mensal = 1 mês de acesso, anual = 12 meses cobrados de uma vez.
    // Mesmos valores exibidos em src/components/sections/Pricing.jsx no frontend.
    private static final Map<PlanType, PlanPricing> PRICES = Map.of(
            PlanType.PRO, new PlanPricing(new BigDecimal("8.00"), new BigDecimal("72.00")),
            PlanType.CONSTELLATION, new PlanPricing(new BigDecimal("24.00"), new BigDecimal("228.00"))
    );

    private record PlanPricing(BigDecimal monthly, BigDecimal annualTotal) {
    }

    @Transactional
    public PreferenceResponse createPreference(String email, PaymentRequest request) {
        if (request.plan() == PlanType.STARTER) {
            throw new InvalidPlanException("Starter is free and does not require checkout");
        }

        User user = getUserByEmail(email);
        BigDecimal amount = resolveAmount(request.plan(), request.billingCycle());

        Payment payment = new Payment();
        payment.setPlan(request.plan());
        payment.setBillingCycle(request.billingCycle());
        payment.setAmount(amount);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        payment.setUser(user);
        payment = paymentRepository.save(payment);

        Preference preference = createMercadoPagoPreference(payment);

        payment.setPreferenceId(preference.getId());
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        return new PreferenceResponse(payment.getId(), preference.getInitPoint());
    }

    public PaymentResponse getById(String email, UUID id) {
        User user = getUserByEmail(email);
        return toResponse(findOwnedPayment(user, id));
    }

    @Transactional
    public void processWebhook(String providerPaymentId) {
        com.mercadopago.resources.payment.Payment mpPayment;
        try {
            PaymentClient client = new PaymentClient();
            mpPayment = client.get(Long.parseLong(providerPaymentId));
        } catch (NumberFormatException e) {
            log.warn("Ignoring webhook with non-numeric payment id: {}", providerPaymentId);
            return;
        } catch (MPApiException | MPException e) {
            throw new PaymentGatewayException("Failed to fetch payment from Mercado Pago", e);
        }

        String externalReference = mpPayment.getExternalReference();
        if (externalReference == null) {
            return;
        }

        Payment payment;
        try {
            payment = paymentRepository.findById(UUID.fromString(externalReference)).orElse(null);
        } catch (IllegalArgumentException e) {
            log.warn("Ignoring webhook with invalid external_reference: {}", externalReference);
            return;
        }
        if (payment == null) {
            log.warn("Payment not found for external_reference: {}", externalReference);
            return;
        }

        PaymentStatus previousStatus = payment.getStatus();
        PaymentStatus newStatus = mapStatus(mpPayment.getStatus());

        payment.setProviderPaymentId(providerPaymentId);
        payment.setStatus(newStatus);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        if (newStatus == PaymentStatus.APPROVED && previousStatus != PaymentStatus.APPROVED) {
            grantPlan(payment);
        }
    }

    private void grantPlan(Payment payment) {
        User user = payment.getUser();
        boolean samePlanStillActive = user.getPlan() == payment.getPlan()
                && user.getPlanExpiresAt() != null
                && user.getPlanExpiresAt().isAfter(LocalDateTime.now());
        LocalDateTime base = samePlanStillActive ? user.getPlanExpiresAt() : LocalDateTime.now();

        user.setPlan(payment.getPlan());
        user.setPlanExpiresAt(
                payment.getBillingCycle() == BillingCycle.ANNUAL ? base.plusYears(1) : base.plusMonths(1));
        userRepository.save(user);
    }

    private Preference createMercadoPagoPreference(Payment payment) {
        PreferenceItemRequest item = PreferenceItemRequest.builder()
                .title("Orbit " + capitalize(payment.getPlan().name()) + " ("
                        + (payment.getBillingCycle() == BillingCycle.ANNUAL ? "Annual" : "Monthly") + ")")
                .quantity(1)
                .currencyId("BRL")
                .unitPrice(payment.getAmount())
                .build();

        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(frontendOrigin + "/checkout/success")
                .pending(frontendOrigin + "/checkout/pending")
                .failure(frontendOrigin + "/checkout/failure")
                .build();

        PreferenceRequest.PreferenceRequestBuilder preferenceRequestBuilder = PreferenceRequest.builder()
                .items(java.util.List.of(item))
                .backUrls(backUrls)
                .autoReturn("approved")
                .externalReference(payment.getId().toString());

        if (notificationUrl != null && !notificationUrl.isBlank()) {
            preferenceRequestBuilder.notificationUrl(notificationUrl + "/payments/webhook");
        }

        try {
            return new PreferenceClient().create(preferenceRequestBuilder.build());
        } catch (MPApiException | MPException e) {
            throw new PaymentGatewayException("Failed to create Mercado Pago preference", e);
        }
    }

    private BigDecimal resolveAmount(PlanType plan, BillingCycle cycle) {
        PlanPricing pricing = PRICES.get(plan);
        return cycle == BillingCycle.ANNUAL ? pricing.annualTotal() : pricing.monthly();
    }

    private PaymentStatus mapStatus(String mpStatus) {
        if (mpStatus == null) return PaymentStatus.PENDING;
        return switch (mpStatus) {
            case "approved" -> PaymentStatus.APPROVED;
            case "rejected" -> PaymentStatus.REJECTED;
            case "cancelled", "refunded", "charged_back" -> PaymentStatus.CANCELLED;
            default -> PaymentStatus.PENDING;
        };
    }

    private String capitalize(String value) {
        return value.charAt(0) + value.substring(1).toLowerCase();
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private Payment findOwnedPayment(User user, UUID id) {
        return paymentRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getPlan(),
                payment.getBillingCycle(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getCreatedAt()
        );
    }
}
