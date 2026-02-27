package com.cotefacil.apigateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpenAPIConfigTest {

    @Test
    void customOpenAPI_ShouldCreateBeanWithExpectedConfiguration() {
        // Given
        OpenAPIConfig config = new OpenAPIConfig();

        // When
        OpenAPI openAPI = config.customOpenAPI();

        // Then
        assertThat(openAPI).isNotNull();

        // Verifica security requirements
        List<SecurityRequirement> securityRequirements = openAPI.getSecurity();
        assertThat(securityRequirements).isNotEmpty();
        assertThat(securityRequirements.get(0).get("bearerAuth")).isNotNull();

        // Verifica security schemes
        assertThat(openAPI.getComponents()).isNotNull();
        SecurityScheme securityScheme = openAPI.getComponents().getSecuritySchemes().get("bearerAuth");
        assertThat(securityScheme).isNotNull();
        assertThat(securityScheme.getType()).isEqualTo(SecurityScheme.Type.HTTP);
        assertThat(securityScheme.getScheme()).isEqualTo("bearer");
        assertThat(securityScheme.getBearerFormat()).isEqualTo("JWT");

        // Verifica info
        Info info = openAPI.getInfo();
        assertThat(info).isNotNull();
        assertThat(info.getTitle()).isEqualTo("API Gateway - CoteFacil");
        assertThat(info.getDescription()).isEqualTo("API responsável por autenticação e roteamento para API de pedidos");
        assertThat(info.getVersion()).isEqualTo("1.0.0");

        Contact contact = info.getContact();
        assertThat(contact).isNotNull();
        assertThat(contact.getName()).isEqualTo("CoteFacil");
        assertThat(contact.getEmail()).isEqualTo("talentos@cotefacil.com");
    }
}
