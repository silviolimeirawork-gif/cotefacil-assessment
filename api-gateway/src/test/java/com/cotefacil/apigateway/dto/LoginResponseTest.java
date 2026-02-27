package com.cotefacil.apigateway.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class LoginResponseTest {

    @Test
    void noArgsConstructor_ShouldCreateEmptyObject() {
        LoginResponse response = new LoginResponse();
        assertThat(response.getToken()).isNull();
    }

    @Test
    void constructorWithToken_ShouldSetToken() {
        String token = "jwt.token.123";
        LoginResponse response = new LoginResponse(token);
        assertThat(response.getToken()).isEqualTo(token);
    }

    @Test
    void setterAndGetter_ShouldWork() {
        LoginResponse response = new LoginResponse();
        String token = "new.token";
        response.setToken(token);
        assertThat(response.getToken()).isEqualTo(token);
    }
}
