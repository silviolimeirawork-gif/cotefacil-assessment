package com.cotefacil.apiorders.controller;

import com.cotefacil.apiorders.dto.OrderRequest;
import com.cotefacil.apiorders.dto.OrderResponse;
import com.cotefacil.apiorders.model.OrderStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser // simula usuário autenticado, já que o token é validado em outro lugar
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateOrder() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setCustomerName("João Silva");
        request.setCustomerEmail("joao@email.com");

        MvcResult result = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.customerName").value("João Silva"))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        OrderResponse order = objectMapper.readValue(response, OrderResponse.class);
        assert order.getId() != null;
    }

    @Test
    void shouldGetOrderById() throws Exception {
        // primeiro cria
        OrderRequest request = new OrderRequest();
        request.setCustomerName("Maria");
        request.setCustomerEmail("maria@email.com");
        MvcResult createResult = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        OrderResponse created = objectMapper.readValue(createResult.getResponse().getContentAsString(), OrderResponse.class);

        mockMvc.perform(get("/api/orders/{id}", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()));
    }
}
