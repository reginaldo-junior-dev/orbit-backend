package reginaldo.orbit.api.doc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import reginaldo.orbit.api.dto.payment.PaymentRequest;
import reginaldo.orbit.api.dto.payment.PaymentResponse;
import reginaldo.orbit.api.dto.payment.PreferenceResponse;

import java.util.UUID;

@Tag(name = OpenApiConfig.TAG_PAYMENTS)
public interface PaymentApi {

    @Operation(summary = "Criar preferência de pagamento",
            description = "Cria um pagamento único (Checkout Pro) para o plano/ciclo escolhido e retorna a URL de checkout do Mercado Pago.")
    ResponseEntity<PreferenceResponse> createPreference(PaymentRequest request, Authentication authentication);

    @Operation(summary = "Consultar pagamento",
            description = "Retorna o status de um pagamento do usuário autenticado. 404 se inexistente ou de outro usuário.")
    ResponseEntity<PaymentResponse> getById(UUID id, Authentication authentication);

    @Operation(summary = "Webhook do Mercado Pago",
            description = "Endpoint público chamado pelo Mercado Pago para notificar mudanças de status de pagamento. A assinatura da notificação é validada via x-signature/x-request-id.")
    ResponseEntity<Void> webhook(String type, String dataId, String xSignature, String xRequestId);
}
