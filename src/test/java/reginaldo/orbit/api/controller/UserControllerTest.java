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
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtService jwtService;

    @Test
    void deveRetornar401AoAcessarMeSemToken() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/users/me")
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void deveRetornar200ComDadosDoUsuarioAutenticado() throws Exception {
        String token = jwtService.gerarToken("user@orbit.com");
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/users/me")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Lucas"))
                .andExpect(jsonPath("$.email").value("user@orbit.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void naoDeveRetornarPasswordNaResposta() throws Exception {
        String token = jwtService.gerarToken("user@orbit.com");
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/users/me")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void deveRetornar200AoAtualizarProprioPerfil() throws Exception {
        String email = "profile-" + UUID.randomUUID() + "@orbit.com";
        String token = registrarELogar(email, "SenhaForte123");

        mockMvc.perform(
                        MockMvcRequestBuilders.put("/users/me").with(csrf())
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "name": "Novo Nome",
                                            "email": "%s"
                                        }
                                        """.formatted(email))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Novo Nome"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void deveRetornar409AoAtualizarEmailParaEmailJaExistente() throws Exception {
        String email = "profile2-" + UUID.randomUUID() + "@orbit.com";
        String token = registrarELogar(email, "SenhaForte123");

        mockMvc.perform(
                        MockMvcRequestBuilders.put("/users/me").with(csrf())
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "name": "Novo Nome",
                                            "email": "admin@orbit.com"
                                        }
                                        """)
                )
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar422AoAtualizarComEmailInvalido() throws Exception {
        String email = "profile3-" + UUID.randomUUID() + "@orbit.com";
        String token = registrarELogar(email, "SenhaForte123");

        mockMvc.perform(
                        MockMvcRequestBuilders.put("/users/me").with(csrf())
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "name": "Novo Nome",
                                            "email": "email-invalido"
                                        }
                                        """)
                )
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.erros.email").isNotEmpty());
    }

    @Test
    void deveRetornar204AoAlterarSenhaELogarComNovaSenha() throws Exception {
        String email = "profile4-" + UUID.randomUUID() + "@orbit.com";
        String token = registrarELogar(email, "SenhaForte123");

        mockMvc.perform(
                        MockMvcRequestBuilders.patch("/users/me/password").with(csrf())
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "currentPassword": "SenhaForte123",
                                            "newPassword": "novaSenha123"
                                        }
                                        """)
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/login").with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "email": "%s",
                                            "password": "novaSenha123"
                                        }
                                        """.formatted(email))
                )
                .andExpect(status().isOk());
    }

    @Test
    void deveRetornar400AoAlterarSenhaComSenhaAtualIncorreta() throws Exception {
        String email = "profile5-" + UUID.randomUUID() + "@orbit.com";
        String token = registrarELogar(email, "SenhaForte123");

        mockMvc.perform(
                        MockMvcRequestBuilders.patch("/users/me/password").with(csrf())
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "currentPassword": "senha-errada",
                                            "newPassword": "novaSenha123"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar204AoExcluirPropriaContaE401AoReutilizarToken() throws Exception {
        String email = "profile6-" + UUID.randomUUID() + "@orbit.com";
        String token = registrarELogar(email, "SenhaForte123");

        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/users/me").with(csrf())
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/users/me")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isUnauthorized());
    }

    private String registrarELogar(String email, String password) throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/register").with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "name": "Usuario Teste",
                                            "email": "%s",
                                            "password": "%s"
                                        }
                                        """.formatted(email, password))
                )
                .andExpect(status().isCreated());

        var response = mockMvc.perform(
                        MockMvcRequestBuilders.post("/login").with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "email": "%s",
                                            "password": "%s"
                                        }
                                        """.formatted(email, password))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        return response.getCookie(AuthController.AUTH_COOKIE_NAME).getValue();
    }
}
