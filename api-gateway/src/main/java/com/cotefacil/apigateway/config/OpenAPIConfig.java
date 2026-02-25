package com.cotefacil.apigateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Gateway - CoteFacil")
                        .description("API responsável por autenticação e roteamento para API de pedidos")
                        .version("1.0.0")
                        .contact(new Contact().name("CoteFacil").email("talentos@cotefacil.com")));
    }
}
