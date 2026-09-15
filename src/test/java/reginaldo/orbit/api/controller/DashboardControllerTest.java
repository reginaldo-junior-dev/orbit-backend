package reginaldo.orbit.api.controller;

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
public class DashboardControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtService jwtService;

    @Test
    void deveRetornar401AoSemToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar200ComEstruturaDoDashboard() throws Exception {
        String email = "dashboard-" + UUID.randomUUID() + "@orbit.com";
        String token = registrarELogar(email, "SenhaForte123");

        mockMvc.perform(MockMvcRequestBuilders.get("/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks.total").isNumber())
                .andExpect(jsonPath("$.tasks.todo").isNumber())
                .andExpect(jsonPath("$.tasks.inProgress").isNumber())
                .andExpect(jsonPath("$.tasks.done").isNumber())
                .andExpect(jsonPath("$.projects.total").isNumber())
                .andExpect(jsonPath("$.projects.active").isNumber())
                .andExpect(jsonPath("$.projects.completed").isNumber())
                .andExpect(jsonPath("$.projects.archived").isNumber())
                .andExpect(jsonPath("$.goals.total").isNumber())
                .andExpect(jsonPath("$.goals.active").isNumber())
                .andExpect(jsonPath("$.goals.completed").isNumber())
                .andExpect(jsonPath("$.goals.archived").isNumber());
    }

    @Test
    void deveRetornarContagensCorretasDoUsuario() throws Exception {
        String email = "dashboard2-" + UUID.randomUUID() + "@orbit.com";
        String token = registrarELogar(email, "SenhaForte123");

        criarTask(token, "TODO");
        criarTask(token, "DONE");

        mockMvc.perform(MockMvcRequestBuilders.post("/projects").with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Projeto dashboard",
                                    "status": "ACTIVE"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(MockMvcRequestBuilders.post("/goals").with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Goal dashboard",
                                    "status": "ACTIVE"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(MockMvcRequestBuilders.get("/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks.total").value(2))
                .andExpect(jsonPath("$.tasks.todo").value(1))
                .andExpect(jsonPath("$.tasks.done").value(1))
                .andExpect(jsonPath("$.projects.total").value(1))
                .andExpect(jsonPath("$.projects.active").value(1))
                .andExpect(jsonPath("$.goals.total").value(1))
                .andExpect(jsonPath("$.goals.active").value(1));
    }

    private void criarTask(String token, String status) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/tasks").with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Task dashboard",
                                    "status": "%s",
                                    "priority": "LOW"
                                }
                                """.formatted(status)))
                .andExpect(status().isCreated());
    }

    private String registrarELogar(String email, String password) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Usuario Dashboard",
                                    "email": "%s",
                                    "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isCreated());

        var response = mockMvc.perform(MockMvcRequestBuilders.post("/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "%s",
                                    "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        return response.getCookie(AuthController.AUTH_COOKIE_NAME).getValue();
    }
}
