package com.cotefacil.apiorders.service;

import com.cotefacil.apiorders.dto.OrderRequest;
import com.cotefacil.apiorders.dto.OrderResponse;
import com.cotefacil.apiorders.exception.ResourceNotFoundException;
import com.cotefacil.apiorders.model.Order;
import com.cotefacil.apiorders.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemService orderItemService;

    @InjectMocks
    private OrderService orderService;

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(1L);
        order.setCustomerName("Test");
        order.setCustomerEmail("test@test.com");
    }

    @Test
    void shouldFindAll() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Order> page = new PageImpl<>(List.of(order));
        when(orderRepository.findAll(pageRequest)).thenReturn(page);

        Page<OrderResponse> result = orderService.findAll(pageRequest);

        assertEquals(1, result.getTotalElements());
        assertEquals("Test", result.getContent().get(0).getCustomerName());
    }

    @Test
    void shouldFindById() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.findById(1L);

        assertEquals("Test", response.getCustomerName());
    }

    @Test
    void shouldThrowWhenNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.findById(99L));
    }

    @Test
    void shouldCreateOrder() {
        OrderRequest request = new OrderRequest();
        request.setCustomerName("New");
        request.setCustomerEmail("new@new.com");

        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(2L);
            return o;
        });

        OrderResponse response = orderService.create(request);

        assertEquals("New", response.getCustomerName());
        assertEquals(2L, response.getId());
    }
}
