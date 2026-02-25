package com.cotefacil.apiorders.config;

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
                        .title("API de Pedidos - CoteFacil")
                        .description("API responsável pelo CRUD de pedidos e itens")
                        .version("1.0.0")
                        .contact(new Contact().name("CoteFacil").email("talentos@cotefacil.com")));
    }
}
