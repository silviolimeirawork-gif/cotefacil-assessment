package com.cotefacil.apiorders.controller;

import com.cotefacil.apiorders.dto.OrderRequest;
import com.cotefacil.apiorders.dto.OrderResponse;
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
@WithMockUser
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createOrder_ShouldReturnCreatedOrder() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setCustomerName("Ana Paula");
        request.setCustomerEmail("ana@email.com");

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.customerName").value("Ana Paula"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void createOrder_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setCustomerName("Ana");
        request.setCustomerEmail("email-invalido");

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOrderById_ExistingId_ShouldReturnOrder() throws Exception {
        // Primeiro cria um pedido
        OrderRequest createRequest = new OrderRequest();
        createRequest.setCustomerName("Carlos");
        createRequest.setCustomerEmail("carlos@email.com");

        MvcResult createResult = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        OrderResponse created = objectMapper.readValue(createResult.getResponse().getContentAsString(), OrderResponse.class);

        mockMvc.perform(get("/api/orders/{id}", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.customerName").value("Carlos"));
    }

    @Test
    void getOrderById_NonExistingId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/orders/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllOrders_ShouldReturnPagedList() throws Exception {
        mockMvc.perform(get("/api/orders")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void updateOrder_ExistingId_ShouldReturnUpdatedOrder() throws Exception {
        // Criar
        OrderRequest createRequest = new OrderRequest();
        createRequest.setCustomerName("Marcos");
        createRequest.setCustomerEmail("marcos@email.com");

        MvcResult createResult = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        OrderResponse created = objectMapper.readValue(createResult.getResponse().getContentAsString(), OrderResponse.class);

        // Atualizar
        OrderRequest updateRequest = new OrderRequest();
        updateRequest.setCustomerName("Marcos Silva");
        updateRequest.setCustomerEmail("marcos.silva@email.com");

        mockMvc.perform(put("/api/orders/{id}", created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").value("Marcos Silva"))
                .andExpect(jsonPath("$.customerEmail").value("marcos.silva@email.com"));
    }

    @Test
    void deleteOrder_ExistingPendingOrder_ShouldReturnNoContent() throws Exception {
        // Criar pedido pendente
        OrderRequest request = new OrderRequest();
        request.setCustomerName("Luiza");
        request.setCustomerEmail("luiza@email.com");

        MvcResult createResult = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        OrderResponse created = objectMapper.readValue(createResult.getResponse().getContentAsString(), OrderResponse.class);

        mockMvc.perform(delete("/api/orders/{id}", created.getId()))
                .andExpect(status().isNoContent());

        // Verificar se foi deletado (GET deve retornar 404)
        mockMvc.perform(get("/api/orders/{id}", created.getId()))
                .andExpect(status().isNotFound());
    }
}
