package br.com.estapar.garagewebhook.infra.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Garage-webhook")
                        .description("API para gerenciamento de vagas e receitas do estacionamento")
                        .contact(new Contact()
                                .name("Edson Ulisses")
                                .email("edsonulissesme@gmail.com")));
    }
}
