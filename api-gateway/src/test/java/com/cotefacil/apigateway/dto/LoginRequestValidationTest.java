package com.cotefacil.apigateway.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LoginRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldBeValidWhenAllFieldsCorrect() {
        LoginRequest request = new LoginRequest();
        request.setUsername("usuario");
        request.setPassword("senha123");

        var violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldBeInvalidWhenUsernameBlank() {
        LoginRequest request = new LoginRequest();
        request.setUsername("");
        request.setPassword("senha123");

        var violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("username", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldBeInvalidWhenPasswordBlank() {
        LoginRequest request = new LoginRequest();
        request.setUsername("usuario");
        request.setPassword("");

        var violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("password", violations.iterator().next().getPropertyPath().toString());
    }
}
