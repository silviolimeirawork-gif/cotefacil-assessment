package com.cotefacil.apigateway.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseTest {

    @Test
    void constructorAndGetters_ShouldSetFieldsCorrectly() {
        int status = 404;
        String error = "Not Found";
        String message = "Resource not found";
        String path = "/api/test";

        ErrorResponse errorResponse = new ErrorResponse(status, error, message, path);

        assertThat(errorResponse.getStatus()).isEqualTo(status);
        assertThat(errorResponse.getError()).isEqualTo(error);
        assertThat(errorResponse.getMessage()).isEqualTo(message);
        assertThat(errorResponse.getPath()).isEqualTo(path);
        assertThat(errorResponse.getTimestamp()).isNotNull(); // timestamp gerado
    }

    @Test
    void settersAndGetters_ShouldWork() {
        ErrorResponse errorResponse = new ErrorResponse(0, null, null, null);

        LocalDateTime now = LocalDateTime.now();
        errorResponse.setTimestamp(now);
        errorResponse.setStatus(500);
        errorResponse.setError("Internal Server Error");
        errorResponse.setMessage("Erro interno");
        errorResponse.setPath("/api/error");

        assertThat(errorResponse.getTimestamp()).isEqualTo(now);
        assertThat(errorResponse.getStatus()).isEqualTo(500);
        assertThat(errorResponse.getError()).isEqualTo("Internal Server Error");
        assertThat(errorResponse.getMessage()).isEqualTo("Erro interno");
        assertThat(errorResponse.getPath()).isEqualTo("/api/error");
    }
}
