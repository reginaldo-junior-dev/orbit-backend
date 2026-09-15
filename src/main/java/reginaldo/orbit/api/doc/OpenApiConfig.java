package reginaldo.orbit.api.doc;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    public static final String TAG_AUTENTICACAO = "Autenticação";
    public static final String TAG_USUARIO = "Usuário";
    public static final String TAG_TASKS = "Tasks";
    public static final String TAG_PROJECTS = "Projects";
    public static final String TAG_GOALS = "Goals";
    public static final String TAG_PAYMENTS = "Payments";
    public static final String TAG_DASHBOARD = "Dashboard";
    public static final String TAG_ADMIN = "Admin";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ORBIT API")
                        .description("SaaS de produtividade para gerenciamento de tarefas, projetos e objetivos.")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT obtido em POST /login")))
                .tags(List.of(
                        new Tag().name(TAG_AUTENTICACAO).description("Registro de conta e autenticação (login com JWT)"),
                        new Tag().name(TAG_USUARIO).description("Perfil do usuário autenticado"),
                        new Tag().name(TAG_TASKS).description("Gestão de tarefas"),
                        new Tag().name(TAG_PROJECTS).description("Gestão de projetos"),
                        new Tag().name(TAG_GOALS).description("Gestão de objetivos"),
                        new Tag().name(TAG_PAYMENTS).description("Checkout e pagamentos via Mercado Pago"),
                        new Tag().name(TAG_DASHBOARD).description("Visão agregada dos dados do usuário"),
                        new Tag().name(TAG_ADMIN).description("Recursos administrativos (apenas ADMIN)")
                ));
    }

    @Bean
    public GroupedOpenApi apiOrbit() {
        return GroupedOpenApi.builder()
                .group("orbit")
                .pathsToMatch("/**")
                .pathsToExclude("/protegido", "/admin")
                .build();
    }
}
