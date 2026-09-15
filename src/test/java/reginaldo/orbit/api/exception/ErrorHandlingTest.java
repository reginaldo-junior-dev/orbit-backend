package reginaldo.orbit.api.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import reginaldo.orbit.api.service.JwtService;

import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class ErrorHandlingTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtService jwtService;

    @Test
    void deveRetornar401ComMensagemPadronizada() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.mensagem").isNotEmpty());
    }

    @Test
    void deveRetornar403ComMensagemPadronizada() throws Exception {
        String token = jwtService.gerarToken("user@orbit.com");
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.mensagem").isNotEmpty());
    }

    @Test
    void deveRetornar400ComUuidInvalido() throws Exception {
        String token = jwtService.gerarToken("user@orbit.com");
        mockMvc.perform(MockMvcRequestBuilders.get("/tasks/nao-e-um-uuid")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").isNotEmpty());
    }

    @Test
    void deveRetornar400ComJsonMalformado() throws Exception {
        String token = jwtService.gerarToken("user@orbit.com");
        mockMvc.perform(MockMvcRequestBuilders.post("/tasks").with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{json-quebrado"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").isNotEmpty());
    }

    @Test
    void deveRetornar404ComMensagemPadronizada() throws Exception {
        String token = jwtService.gerarToken("user@orbit.com");
        mockMvc.perform(MockMvcRequestBuilders.get("/tasks/{id}", UUID.randomUUID())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Task not found"));
    }

    @Test
    void deveRetornar409ComMensagemPadronizada() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Conflito",
                                    "email": "admin@orbit.com",
                                    "password": "SenhaForte123"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensagem").isNotEmpty());
    }

    @Test
    void deveRetornar422ComErrosPorCampo() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "",
                                    "email": "",
                                    "password": ""
                                }
                                """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.mensagem").value("Validation error"))
                .andExpect(jsonPath("$.erros.name").isNotEmpty())
                .andExpect(jsonPath("$.erros.email").isNotEmpty())
                .andExpect(jsonPath("$.erros.password").isNotEmpty());
    }
}
