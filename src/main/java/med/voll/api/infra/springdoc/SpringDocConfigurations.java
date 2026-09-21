package med.voll.api.infra.springdoc;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// @Configuration: avisa ao Spring que essa classe é de configuração.
// Ele lê a classe na inicialização da aplicação e registra os @Bean que estão dentro.
// Analogia: é como o "arquivo de setup" que roda antes de o app começar a atender.
@Configuration
public class SpringDocConfigurations {

    // @Bean: o objeto retornado por este metodo fica registrado no Spring
    // e o SpringDoc (biblioteca que gera o Swagger) o usa para montar a documentação.
    // Ou seja: aqui você personaliza o que aparece na tela do Swagger UI.
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()

                // ---- SEGURANÇA ----
                // Registra um "esquema de segurança" chamado "bearer-key".
                // Isso faz aparecer o botão "Authorize" no Swagger UI, onde você cola o token JWT.
                // Analogia: é o crachá que você mostra na portaria antes de acessar os endpoints.
                .components(new Components()
                        .addSecuritySchemes("bearer-key",       // nome/ID do esquema (é esse nome que você referencia nos controllers)
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)  // autenticação via cabeçalho HTTP
                                        .scheme("bearer")                // formato: "Authorization: Bearer <token>"
                                        .bearerFormat("JWT")))           // só informativo: diz que o token é um JWT

                // ---- INFORMAÇÕES DA API ----
                // Tudo daqui pra baixo é só "cartão de visita" da documentação.
                // Não altera o comportamento da API, apenas o que é exibido no Swagger.
                .info(new Info()
                        .title("Voll.med API")  // título exibido no topo do Swagger
                        .description("API Rest da aplicação Voll.med, contendo as funcionalidades de CRUD de médicos e de pacientes, além de agendamento e cancelamento de consultas")
                        .contact(new Contact()  // quem procurar em caso de dúvida
                                .name("Time Backend")
                                .email("backend@voll.med"))
                        .license(new License()  // licença de uso da API
                                .name("Apache 2.0")
                                .url("http://voll.med/api/licenca")));
    }
}