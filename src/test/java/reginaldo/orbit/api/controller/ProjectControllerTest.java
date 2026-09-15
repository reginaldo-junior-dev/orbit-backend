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
public class ProjectControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtService jwtService;

    private static final String USER_TOKEN_PREFIX = "Bearer ";

    @Test
    void deveRetornar401AoListarProjetosSemToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/projects"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar201AoCriarProjeto() throws Exception {
        String token = jwtService.gerarToken("user@orbit.com");
        mockMvc.perform(MockMvcRequestBuilders.post("/projects").with(csrf())
                        .header("Authorization", USER_TOKEN_PREFIX + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Projeto teste",
                                    "description": "Descrição do projeto",
                                    "status": "ACTIVE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Projeto teste"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void deveRetornar422AoCriarProjetoSemNome() throws Exception {
        String token = jwtService.gerarToken("user@orbit.com");
        mockMvc.perform(MockMvcRequestBuilders.post("/projects").with(csrf())
                        .header("Authorization", USER_TOKEN_PREFIX + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "",
                                    "status": "ACTIVE"
                                }
                                """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.erros.name").isNotEmpty());
    }

    @Test
    void deveRetornar200AoListarSomenteProjetosDoUsuario() throws Exception {
        String userToken = jwtService.gerarToken("user@orbit.com");
        String adminToken = jwtService.gerarToken("admin@orbit.com");

        String projetoDoUsuario = "Projeto do usuario " + UUID.randomUUID();
        String projetoDoAdmin = "Projeto do admin " + UUID.randomUUID();

        mockMvc.perform(MockMvcRequestBuilders.post("/projects").with(csrf())
                        .header("Authorization", USER_TOKEN_PREFIX + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "%s",
                                    "status": "ACTIVE"
                                }
                                """.formatted(projetoDoUsuario)))
                .andExpect(status().isCreated());

        mockMvc.perform(MockMvcRequestBuilders.post("/projects").with(csrf())
                        .header("Authorization", USER_TOKEN_PREFIX + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "%s",
                                    "status": "ACTIVE"
                                }
                                """.formatted(projetoDoAdmin)))
                .andExpect(status().isCreated());

        mockMvc.perform(MockMvcRequestBuilders.get("/projects")
                        .header("Authorization", USER_TOKEN_PREFIX + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name").value(Matchers.hasItem(projetoDoUsuario)))
                .andExpect(jsonPath("$[*].name").value(not(Matchers.hasItem(projetoDoAdmin))));
    }

    @Test
    void deveRetornar200AoBuscarProjetoProprio() throws Exception {
        String token = jwtService.gerarToken("user@orbit.com");
        String id = criarProjeto(token, "Projeto proprio " + UUID.randomUUID());

        mockMvc.perform(MockMvcRequestBuilders.get("/projects/{id}", id)
                        .header("Authorization", USER_TOKEN_PREFIX + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void deveRetornar404AoBuscarProjetoDeOutroUsuario() throws Exception {
        String adminToken = jwtService.gerarToken("admin@orbit.com");
        String id = criarProjeto(adminToken, "Projeto do admin " + UUID.randomUUID());

        String userToken = jwtService.gerarToken("user@orbit.com");
        mockMvc.perform(MockMvcRequestBuilders.get("/projects/{id}", id)
                        .header("Authorization", USER_TOKEN_PREFIX + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar404AoBuscarProjetoInexistente() throws Exception {
        String token = jwtService.gerarToken("user@orbit.com");
        mockMvc.perform(MockMvcRequestBuilders.get("/projects/{id}", UUID.randomUUID())
                        .header("Authorization", USER_TOKEN_PREFIX + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar200AoAtualizarProjetoProprio() throws Exception {
        String token = jwtService.gerarToken("user@orbit.com");
        String id = criarProjeto(token, "Projeto antes " + UUID.randomUUID());

        mockMvc.perform(MockMvcRequestBuilders.put("/projects/{id}", id).with(csrf())
                        .header("Authorization", USER_TOKEN_PREFIX + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Projeto atualizado",
                                    "description": "Nova descricao",
                                    "status": "COMPLETED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Projeto atualizado"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void deveRetornar404AoAtualizarProjetoDeOutroUsuario() throws Exception {
        String adminToken = jwtService.gerarToken("admin@orbit.com");
        String id = criarProjeto(adminToken, "Projeto do admin " + UUID.randomUUID());

        String userToken = jwtService.gerarToken("user@orbit.com");
        mockMvc.perform(MockMvcRequestBuilders.put("/projects/{id}", id).with(csrf())
                        .header("Authorization", USER_TOKEN_PREFIX + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Invasao",
                                    "status": "ACTIVE"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar204AoExcluirProjetoProprio() throws Exception {
        String token = jwtService.gerarToken("user@orbit.com");
        String id = criarProjeto(token, "Projeto para excluir " + UUID.randomUUID());

        mockMvc.perform(MockMvcRequestBuilders.delete("/projects/{id}", id).with(csrf())
                        .header("Authorization", USER_TOKEN_PREFIX + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(MockMvcRequestBuilders.get("/projects/{id}", id)
                        .header("Authorization", USER_TOKEN_PREFIX + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar404AoExcluirProjetoDeOutroUsuario() throws Exception {
        String adminToken = jwtService.gerarToken("admin@orbit.com");
        String id = criarProjeto(adminToken, "Projeto do admin " + UUID.randomUUID());

        String userToken = jwtService.gerarToken("user@orbit.com");
        mockMvc.perform(MockMvcRequestBuilders.delete("/projects/{id}", id).with(csrf())
                        .header("Authorization", USER_TOKEN_PREFIX + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar409AoExcluirProjetoComTarefas() throws Exception {
        String token = jwtService.gerarToken("user@orbit.com");
        String projectId = criarProjeto(token, "Projeto com tarefas " + UUID.randomUUID());

        mockMvc.perform(MockMvcRequestBuilders.post("/tasks").with(csrf())
                        .header("Authorization", USER_TOKEN_PREFIX + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Tarefa do projeto",
                                    "status": "TODO",
                                    "priority": "LOW",
                                    "projectId": "%s"
                                }
                                """.formatted(projectId)))
                .andExpect(status().isCreated());

        mockMvc.perform(MockMvcRequestBuilders.delete("/projects/{id}", projectId).with(csrf())
                        .header("Authorization", USER_TOKEN_PREFIX + token))
                .andExpect(status().isConflict());
    }

    private String criarProjeto(String token, String name) throws Exception {
        String response = mockMvc.perform(MockMvcRequestBuilders.post("/projects").with(csrf())
                        .header("Authorization", USER_TOKEN_PREFIX + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "%s",
                                    "status": "ACTIVE"
                                }
                                """.formatted(name)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return com.jayway.jsonpath.JsonPath.read(response, "$.id");
    }
}
