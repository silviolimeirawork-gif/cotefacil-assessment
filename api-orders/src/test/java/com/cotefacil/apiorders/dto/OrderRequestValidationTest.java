package com.cotefacil.apiorders.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class OrderRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldBeValidWhenAllFieldsCorrect() {
        OrderRequest request = new OrderRequest();
        request.setCustomerName("João");
        request.setCustomerEmail("joao@email.com");

        var violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldBeInvalidWhenCustomerNameBlank() {
        OrderRequest request = new OrderRequest();
        request.setCustomerName("");
        request.setCustomerEmail("joao@email.com");

        var violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("customerName", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldBeInvalidWhenEmailInvalid() {
        OrderRequest request = new OrderRequest();
        request.setCustomerName("João");
        request.setCustomerEmail("email-invalido");

        var violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("customerEmail", violations.iterator().next().getPropertyPath().toString());
    }
}
