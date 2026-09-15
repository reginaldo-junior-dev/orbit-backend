package reginaldo.orbit.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import reginaldo.orbit.api.service.JwtService;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class PaymentControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtService jwtService;

    private static final String USER_TOKEN_PREFIX = "Bearer ";

    @Test
    void deveRetornar401AoCriarPreferenciaSemToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/payments/preference").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "plan": "PRO",
                                    "billingCycle": "MONTHLY"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar422AoCriarPreferenciaSemPlano() throws Exception {
        String token = jwtService.gerarToken("user@orbit.com");
        mockMvc.perform(MockMvcRequestBuilders.post("/payments/preference").with(csrf())
                        .header("Authorization", USER_TOKEN_PREFIX + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "billingCycle": "MONTHLY"
                                }
                                """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.erros.plan").isNotEmpty());
    }

    @Test
    void deveRetornar401AoBuscarPagamentoSemToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/payments/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar404AoBuscarPagamentoInexistente() throws Exception {
        String token = jwtService.gerarToken("user@orbit.com");
        mockMvc.perform(MockMvcRequestBuilders.get("/payments/{id}", UUID.randomUUID())
                        .header("Authorization", USER_TOKEN_PREFIX + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveIgnorarWebhookDeTipoDiferenteDePayment() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/payments/webhook")
                        .param("type", "merchant_order")
                        .param("data.id", "123456"))
                .andExpect(status().isOk());
    }

    @Test
    void deveRetornar401NoWebhookComAssinaturaInvalida() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/payments/webhook")
                        .param("type", "payment")
                        .param("data.id", "123456")
                        .header("x-signature", "ts=1,v1=invalid")
                        .header("x-request-id", "req-1"))
                .andExpect(status().isUnauthorized());
    }
}
