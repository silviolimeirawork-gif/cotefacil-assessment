package com.cotefacil.apiorders.controller;

import com.cotefacil.apiorders.dto.OrderItemRequest;
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

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class OrderItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Long createTestOrder() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setCustomerName("Cliente Item");
        request.setCustomerEmail("item@cliente.com");
        MvcResult result = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        OrderResponse order = objectMapper.readValue(result.getResponse().getContentAsString(), OrderResponse.class);
        return order.getId();
    }

    @Test
    void addItemToOrder_ShouldReturnCreatedItem() throws Exception {
        Long orderId = createTestOrder();

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductName("Notebook");
        itemRequest.setQuantity(1);
        itemRequest.setUnitPrice(BigDecimal.valueOf(2500));

        mockMvc.perform(post("/api/orders/{orderId}/items", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.productName").value("Notebook"))
                .andExpect(jsonPath("$.subtotal").value(2500));
    }

    @Test
    void listItemsFromOrder_ShouldReturnList() throws Exception {
        Long orderId = createTestOrder();

        // Adicionar um item
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductName("Mouse");
        itemRequest.setQuantity(2);
        itemRequest.setUnitPrice(BigDecimal.valueOf(50));
        mockMvc.perform(post("/api/orders/{orderId}/items", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequest)))
                .andExpect(status().isCreated());

        // Listar
        mockMvc.perform(get("/api/orders/{orderId}/items", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productName").value("Mouse"));
    }

    @Test
    void addItemToNonExistingOrder_ShouldReturnNotFound() throws Exception {
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductName("Produto");
        itemRequest.setQuantity(1);
        itemRequest.setUnitPrice(BigDecimal.TEN);

        mockMvc.perform(post("/api/orders/999/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequest)))
                .andExpect(status().isNotFound());
    }
}
