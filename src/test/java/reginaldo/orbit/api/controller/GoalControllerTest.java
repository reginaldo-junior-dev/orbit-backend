package reginaldo.orbit.api.controller;

import org.hamcrest.Matchers;
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

import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class GoalControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtService jwtService;

    private static final String USER_TOKEN_PREFIX = "Bearer ";

    @Test
    void deveRetornar401AoListarGoalsSemToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/goals"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar201AoCriarGoal() throws Exception {
        String token = jwtService.gerarToken("user@orbit.com");
        mockMvc.perform(MockMvcRequestBuilders.post("/goals").with(csrf())
                        .header("Authorization", USER_TOKEN_PREFIX + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Goal teste",
                                    "description": "Descrição da goal",
                                    "status": "ACTIVE",
                                    "targetDate": "2027-06-30"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Goal teste"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.targetDate").value("2027-06-30"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void deveRetornar422AoCriarGoalSemTitulo() throws Exception {
        String token = jwtService.gerarToken("user@orbit.com");
        mockMvc.perform(MockMvcRequestBuilders.post("/goals").with(csrf())
                        .header("Authorization", USER_TOKEN_PREFIX + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "",
                                    "status": "ACTIVE"
                                }
                                """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.erros.title").isNotEmpty());
    }

    @Test
    void deveRetornar200AoListarSomenteGoalsDoUsuario() throws Exception {
        String userToken = jwtService.gerarToken("user@orbit.com");
        String adminToken = jwtService.gerarToken("admin@orbit.com");

        String goalDoUsuario = "Goal do usuario " + UUID.randomUUID();
        String goalDoAdmin = "Goal do admin " + UUID.randomUUID();

        mockMvc.perform(MockMvcRequestBuilders.post("/goals").with(csrf())
                        .header("Authorization", USER_TOKEN_PREFIX + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "%s",
                                    "status": "ACTIVE"
                                }
                                """.formatted(goalDoUsuario)))
                .andExpect(status().isCreated());

        mockMvc.perform(MockMvcRequestBuilders.post("/goals").with(csrf())
                        .header("Authorization", USER_TOKEN_PREFIX + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "%s",
                                    "status": "ACTIVE"
                                }
                                """.formatted(goalDoAdmin)))
                .andExpect(status().isCreated());

        mockMvc.perform(MockMvcRequestBuilders.get("/goals")
                        .header("Authorization", USER_TOKEN_PREFIX + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title").value(Matchers.hasItem(goalDoUsuario)))
                .andExpect(jsonPath("$[*].title").value(not(Matchers.hasItem(goalDoAdmin))));
    }

    @Test
    void deveRetornar200AoBuscarGoalPropria() throws Exception {
        String token = jwtService.gerarToken("user@orbit.com");
        String id = criarGoal(token, "Goal propria " + UUID.randomUUID());

        mockMvc.perform(MockMvcRequestBuilders.get("/goals/{id}", id)
                        .header("Authorization", USER_TOKEN_PREFIX + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void deveRetornar404AoBuscarGoalDeOutroUsuario() throws Exception {
        String adminToken = jwtService.gerarToken("admin@orbit.com");
        String id = criarGoal(adminToken, "Goal do admin " + UUID.randomUUID());

        String userToken = jwtService.gerarToken("user@orbit.com");
        mockMvc.perform(MockMvcRequestBuilders.get("/goals/{id}", id)
                        .header("Authorization", USER_TOKEN_PREFIX + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar404AoBuscarGoalInexistente() throws Exception {
        String token = jwtService.gerarToken("user@orbit.com");
        mockMvc.perform(MockMvcRequestBuilders.get("/goals/{id}", UUID.randomUUID())
                        .header("Authorization", USER_TOKEN_PREFIX + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar200AoAtualizarGoalPropria() throws Exception {
        String token = jwtService.gerarToken("user@orbit.com");
        String id = criarGoal(token, "Goal antes " + UUID.randomUUID());

        mockMvc.perform(MockMvcRequestBuilders.put("/goals/{id}", id).with(csrf())
                        .header("Authorization", USER_TOKEN_PREFIX + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Goal atualizada",
                                    "description": "Nova descricao",
                                    "status": "COMPLETED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Goal atualizada"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void deveRetornar404AoAtualizarGoalDeOutroUsuario() throws Exception {
        String adminToken = jwtService.gerarToken("admin@orbit.com");
        String id = criarGoal(adminToken, "Goal do admin " + UUID.randomUUID());

        String userToken = jwtService.gerarToken("user@orbit.com");
        mockMvc.perform(MockMvcRequestBuilders.put("/goals/{id}", id).with(csrf())
                        .header("Authorization", USER_TOKEN_PREFIX + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Invasao",
                                    "status": "ACTIVE"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar204AoExcluirGoalPropria() throws Exception {
        String token = jwtService.gerarToken("user@orbit.com");
        String id = criarGoal(token, "Goal para excluir " + UUID.randomUUID());

        mockMvc.perform(MockMvcRequestBuilders.delete("/goals/{id}", id).with(csrf())
                        .header("Authorization", USER_TOKEN_PREFIX + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(MockMvcRequestBuilders.get("/goals/{id}", id)
                        .header("Authorization", USER_TOKEN_PREFIX + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar404AoExcluirGoalDeOutroUsuario() throws Exception {
        String adminToken = jwtService.gerarToken("admin@orbit.com");
        String id = criarGoal(adminToken, "Goal do admin " + UUID.randomUUID());

        String userToken = jwtService.gerarToken("user@orbit.com");
        mockMvc.perform(MockMvcRequestBuilders.delete("/goals/{id}", id).with(csrf())
                        .header("Authorization", USER_TOKEN_PREFIX + userToken))
                .andExpect(status().isNotFound());
    }

    private String criarGoal(String token, String title) throws Exception {
        String response = mockMvc.perform(MockMvcRequestBuilders.post("/goals").with(csrf())
                        .header("Authorization", USER_TOKEN_PREFIX + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "%s",
                                    "status": "ACTIVE"
                                }
                                """.formatted(title)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return com.jayway.jsonpath.JsonPath.read(response, "$.id");
    }
}
