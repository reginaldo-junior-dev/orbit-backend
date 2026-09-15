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
public class TaskControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtService jwtService;

    private static final String USER_TOKEN_PREFIX = "Bearer ";

    @Test
    void deveRetornar401AoListarTarefasSemToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar201AoCriarTarefa() throws Exception {
        String token = jwtService.gerarToken("user@orbit.com");
        mockMvc.perform(MockMvcRequestBuilders.post("/tasks").with(csrf())
                        .header("Authorization", USER_TOKEN_PREFIX + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Tarefa teste",
                                    "description": "Descrição da tarefa",
                                    "status": "TODO",
                                    "priority": "HIGH",
                                    "dueDate": "2026-12-31"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Tarefa teste"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.dueDate").value("2026-12-31"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void deveRetornar422AoCriarTarefaSemTitulo() throws Exception {
        String token = jwtService.gerarToken("user@orbit.com");
        mockMvc.perform(MockMvcRequestBuilders.post("/tasks").with(csrf())
                        .header("Authorization", USER_TOKEN_PREFIX + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "",
                                    "status": "TODO",
                                    "priority": "LOW"
                                }
                                """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.erros.title").isNotEmpty());
    }

    @Test
    void deveRetornar200AoListarSomenteTarefasDoUsuario() throws Exception {
        String userToken = jwtService.gerarToken("user@orbit.com");
        String adminToken = jwtService.gerarToken("admin@orbit.com");

        String tarefaDoUsuario = "Tarefa do usuario " + UUID.randomUUID();
        String tarefaDoAdmin = "Tarefa do admin " + UUID.randomUUID();

        mockMvc.perform(MockMvcRequestBuilders.post("/tasks").with(csrf())
                        .header("Authorization", USER_TOKEN_PREFIX + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "%s",
                                    "status": "TODO",
                                    "priority": "LOW"
                                }
                                """.formatted(tarefaDoUsuario)))
                .andExpect(status().isCreated());

        mockMvc.perform(MockMvcRequestBuilders.post("/tasks").with(csrf())
                        .header("Authorization", USER_TOKEN_PREFIX + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "%s",
                                    "status": "TODO",
                                    "priority": "LOW"
                                }
                                """.formatted(tarefaDoAdmin)))
                .andExpect(status().isCreated());

        mockMvc.perform(MockMvcRequestBuilders.get("/tasks")
                        .header("Authorization", USER_TOKEN_PREFIX + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title").value(Matchers.hasItem(tarefaDoUsuario)))
                .andExpect(jsonPath("$[*].title").value(not(Matchers.hasItem(tarefaDoAdmin))));
    }

    @Test
    void deveRetornar200AoBuscarTarefaPropria() throws Exception {
        String token = jwtService.gerarToken("user@orbit.com");
        String id = criarTarefa(token, "Tarefa propria " + UUID.randomUUID());

        mockMvc.perform(MockMvcRequestBuilders.get("/tasks/{id}", id)
                        .header("Authorization", USER_TOKEN_PREFIX + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void deveRetornar404AoBuscarTarefaDeOutroUsuario() throws Exception {
        String adminToken = jwtService.gerarToken("admin@orbit.com");
        String id = criarTarefa(adminToken, "Tarefa do admin " + UUID.randomUUID());

        String userToken = jwtService.gerarToken("user@orbit.com");
        mockMvc.perform(MockMvcRequestBuilders.get("/tasks/{id}", id)
                        .header("Authorization", USER_TOKEN_PREFIX + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar404AoBuscarTarefaInexistente() throws Exception {
        String token = jwtService.gerarToken("user@orbit.com");
        mockMvc.perform(MockMvcRequestBuilders.get("/tasks/{id}", UUID.randomUUID())
                        .header("Authorization", USER_TOKEN_PREFIX + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar200AoAtualizarTarefaPropria() throws Exception {
        String token = jwtService.gerarToken("user@orbit.com");
        String id = criarTarefa(token, "Tarefa antes " + UUID.randomUUID());

        mockMvc.perform(MockMvcRequestBuilders.put("/tasks/{id}", id).with(csrf())
                        .header("Authorization", USER_TOKEN_PREFIX + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Tarefa atualizada",
                                    "description": "Nova descricao",
                                    "status": "DONE",
                                    "priority": "MEDIUM"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Tarefa atualizada"))
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"));
    }

    @Test
    void deveRetornar404AoAtualizarTarefaDeOutroUsuario() throws Exception {
        String adminToken = jwtService.gerarToken("admin@orbit.com");
        String id = criarTarefa(adminToken, "Tarefa do admin " + UUID.randomUUID());

        String userToken = jwtService.gerarToken("user@orbit.com");
        mockMvc.perform(MockMvcRequestBuilders.put("/tasks/{id}", id).with(csrf())
                        .header("Authorization", USER_TOKEN_PREFIX + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Invasao",
                                    "status": "TODO",
                                    "priority": "LOW"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar204AoExcluirTarefaPropria() throws Exception {
        String token = jwtService.gerarToken("user@orbit.com");
        String id = criarTarefa(token, "Tarefa para excluir " + UUID.randomUUID());

        mockMvc.perform(MockMvcRequestBuilders.delete("/tasks/{id}", id).with(csrf())
                        .header("Authorization", USER_TOKEN_PREFIX + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(MockMvcRequestBuilders.get("/tasks/{id}", id)
                        .header("Authorization", USER_TOKEN_PREFIX + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar404AoExcluirTarefaDeOutroUsuario() throws Exception {
        String adminToken = jwtService.gerarToken("admin@orbit.com");
        String id = criarTarefa(adminToken, "Tarefa do admin " + UUID.randomUUID());

        String userToken = jwtService.gerarToken("user@orbit.com");
        mockMvc.perform(MockMvcRequestBuilders.delete("/tasks/{id}", id).with(csrf())
                        .header("Authorization", USER_TOKEN_PREFIX + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar201AoCriarTarefaAssociadaAProjetoProprio() throws Exception {
        String token = jwtService.gerarToken("user@orbit.com");
        String projectId = criarProjeto(token, "Projeto da tarefa " + UUID.randomUUID());

        mockMvc.perform(MockMvcRequestBuilders.post("/tasks").with(csrf())
                        .header("Authorization", USER_TOKEN_PREFIX + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Tarefa com projeto",
                                    "status": "TODO",
                                    "priority": "MEDIUM",
                                    "projectId": "%s"
                                }
                                """.formatted(projectId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projectId").value(projectId));
    }

    @Test
    void deveRetornar201AoCriarTarefaSemProjeto() throws Exception {
        String token = jwtService.gerarToken("user@orbit.com");
        mockMvc.perform(MockMvcRequestBuilders.post("/tasks").with(csrf())
                        .header("Authorization", USER_TOKEN_PREFIX + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Tarefa sem projeto",
                                    "status": "TODO",
                                    "priority": "LOW"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projectId").doesNotExist());
    }

    @Test
    void deveRetornar404AoCriarTarefaComProjetoDeOutroUsuario() throws Exception {
        String adminToken = jwtService.gerarToken("admin@orbit.com");
        String projectId = criarProjeto(adminToken, "Projeto do admin " + UUID.randomUUID());

        String userToken = jwtService.gerarToken("user@orbit.com");
        mockMvc.perform(MockMvcRequestBuilders.post("/tasks").with(csrf())
                        .header("Authorization", USER_TOKEN_PREFIX + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Tarefa invasora",
                                    "status": "TODO",
                                    "priority": "LOW",
                                    "projectId": "%s"
                                }
                                """.formatted(projectId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar404AoCriarTarefaComProjetoInexistente() throws Exception {
        String token = jwtService.gerarToken("user@orbit.com");
        mockMvc.perform(MockMvcRequestBuilders.post("/tasks").with(csrf())
                        .header("Authorization", USER_TOKEN_PREFIX + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Tarefa com projeto fantasma",
                                    "status": "TODO",
                                    "priority": "LOW",
                                    "projectId": "%s"
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar200AoAssociarTarefaAProjetoProprio() throws Exception {
        String token = jwtService.gerarToken("user@orbit.com");
        String taskId = criarTarefa(token, "Tarefa para associar " + UUID.randomUUID());
        String projectId = criarProjeto(token, "Projeto alvo " + UUID.randomUUID());

        mockMvc.perform(MockMvcRequestBuilders.put("/tasks/{id}", taskId).with(csrf())
                        .header("Authorization", USER_TOKEN_PREFIX + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Tarefa associada",
                                    "status": "TODO",
                                    "priority": "LOW",
                                    "projectId": "%s"
                                }
                                """.formatted(projectId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(projectId));
    }

    @Test
    void deveRetornar404AoAssociarTarefaAProjetoDeOutroUsuario() throws Exception {
        String adminToken = jwtService.gerarToken("admin@orbit.com");
        String projectId = criarProjeto(adminToken, "Projeto do admin " + UUID.randomUUID());

        String userToken = jwtService.gerarToken("user@orbit.com");
        String taskId = criarTarefa(userToken, "Tarefa do usuario " + UUID.randomUUID());

        mockMvc.perform(MockMvcRequestBuilders.put("/tasks/{id}", taskId).with(csrf())
                        .header("Authorization", USER_TOKEN_PREFIX + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Tarefa associada",
                                    "status": "TODO",
                                    "priority": "LOW",
                                    "projectId": "%s"
                                }
                                """.formatted(projectId)))
                .andExpect(status().isNotFound());
    }

    private String criarTarefa(String token, String title) throws Exception {
        String response = mockMvc.perform(MockMvcRequestBuilders.post("/tasks").with(csrf())
                        .header("Authorization", USER_TOKEN_PREFIX + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "%s",
                                    "status": "TODO",
                                    "priority": "LOW"
                                }
                                """.formatted(title)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return com.jayway.jsonpath.JsonPath.read(response, "$.id");
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
