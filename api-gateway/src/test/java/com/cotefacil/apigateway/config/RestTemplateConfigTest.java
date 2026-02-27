package com.cotefacil.apigateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class RestTemplateConfigTest {

    @Test
    void restTemplate_ShouldCreateBean() {
        RestTemplateConfig config = new RestTemplateConfig();
        RestTemplate restTemplate = config.restTemplate();
        assertThat(restTemplate).isNotNull();
    }
}
