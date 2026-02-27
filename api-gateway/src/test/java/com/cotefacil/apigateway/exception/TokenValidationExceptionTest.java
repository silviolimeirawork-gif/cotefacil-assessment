package com.cotefacil.apigateway.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class TokenValidationExceptionTest {

    @Test
    void constructor_ShouldSetMessage() {
        String message = "Token inválido";
        TokenValidationException exception = new TokenValidationException(message);
        assertThat(exception.getMessage()).isEqualTo(message);
    }
}
