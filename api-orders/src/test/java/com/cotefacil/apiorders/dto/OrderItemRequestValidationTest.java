package com.cotefacil.apiorders.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class OrderItemRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldBeValidWhenAllFieldsCorrect() {
        OrderItemRequest request = new OrderItemRequest();
        request.setProductName("Produto");
        request.setQuantity(2);
        request.setUnitPrice(BigDecimal.TEN);

        var violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldBeInvalidWhenProductNameBlank() {
        OrderItemRequest request = new OrderItemRequest();
        request.setProductName("");
        request.setQuantity(1);
        request.setUnitPrice(BigDecimal.ONE);

        var violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("productName", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldBeInvalidWhenQuantityLessThanOne() {
        OrderItemRequest request = new OrderItemRequest();
        request.setProductName("Produto");
        request.setQuantity(0);
        request.setUnitPrice(BigDecimal.ONE);

        var violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("quantity", violations.iterator().next().getPropertyPath().toString());
    }
}
