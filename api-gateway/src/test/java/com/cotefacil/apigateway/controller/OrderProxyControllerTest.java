package com.cotefacil.apigateway.controller;

import com.cotefacil.apigateway.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderProxyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private RestTemplate restTemplate;

    private String validToken;

    @BeforeEach
    void setUp() {
        User user = (User) User.withUsername("usuario").password("").authorities("USER").build();
        validToken = jwtService.generateToken(user);
    }

    @Test
    void shouldProxyGetRequest() throws Exception {
        String responseBody = "{\"id\":1,\"name\":\"test\"}";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        mockMvc.perform(get("/api/orders/1")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(content().json(responseBody));
    }

    @Test
    void shouldProxyPostRequest() throws Exception {
        String requestBody = "{\"customerName\":\"João\"}";
        String responseBody = "{\"id\":1,\"customerName\":\"João\"}";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.CREATED);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().json(responseBody));
    }

    @Test
    void shouldReturnErrorWhenApi2Fails() throws Exception {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new RuntimeException("API 2 error"));

        mockMvc.perform(get("/api/orders/1")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isInternalServerError()); // tratado pelo GlobalExceptionHandler
    }
}
