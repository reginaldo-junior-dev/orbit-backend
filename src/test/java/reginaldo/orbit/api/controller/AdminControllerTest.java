package reginaldo.orbit.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import reginaldo.orbit.api.entity.User;
import reginaldo.orbit.api.repository.UserRepository;
import reginaldo.orbit.api.service.JwtService;

import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepository;

    @Test
    void deveRetornar401AoAcessarAdminSemToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar403AoListarUsuariosComoUsuarioComum() throws Exception {
        String token = jwtService.gerarToken("user@orbit.com");
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRetornar403AoBuscarUsuarioComoUsuarioComum() throws Exception {
        String token = jwtService.gerarToken("user@orbit.com");
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/users/{id}", UUID.randomUUID())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRetornar200AoListarUsuariosComoAdmin() throws Exception {
        String token = jwtService.gerarToken("admin@orbit.com");
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].email").value(hasItem("user@orbit.com")))
                .andExpect(jsonPath("$[*].email").value(hasItem("admin@orbit.com")));
    }

    @Test
    void deveRetornar200AoBuscarUsuarioComoAdminSemPassword() throws Exception {
        User user = userRepository.findByEmail("user@orbit.com").orElseThrow();
        String token = jwtService.gerarToken("admin@orbit.com");

        mockMvc.perform(MockMvcRequestBuilders.get("/admin/users/{id}", user.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId().toString()))
                .andExpect(jsonPath("$.name").value("Lucas"))
                .andExpect(jsonPath("$.email").value("user@orbit.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void deveRetornar404AoBuscarUsuarioInexistenteComoAdmin() throws Exception {
        String token = jwtService.gerarToken("admin@orbit.com");
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/users/{id}", UUID.randomUUID())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}
