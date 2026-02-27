package com.cotefacil.apigateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

class CorsConfigTest {

    @Test
    void corsConfigurationSource_ShouldCreateBean() {
        CorsConfig config = new CorsConfig();
        CorsConfigurationSource source = config.corsConfigurationSource();
        assertThat(source).isNotNull();
    }
}
