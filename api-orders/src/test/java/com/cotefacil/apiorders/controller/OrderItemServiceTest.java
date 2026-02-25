package com.cotefacil.apiorders.service;

import com.cotefacil.apiorders.dto.OrderItemRequest;
import com.cotefacil.apiorders.dto.OrderItemResponse;
import com.cotefacil.apiorders.exception.BusinessException;
import com.cotefacil.apiorders.exception.ResourceNotFoundException;
import com.cotefacil.apiorders.model.Order;
import com.cotefacil.apiorders.model.OrderItem;
import com.cotefacil.apiorders.model.OrderStatus;
import com.cotefacil.apiorders.repository.OrderItemRepository;
import com.cotefacil.apiorders.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderItemService orderItemService;

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(1L);
        order.setCustomerName("Test");
        order.setCustomerEmail("test@test.com");
        order.setStatus(OrderStatus.PENDING);
    }

    @Test
    void findAllByOrderId_ShouldReturnItems() {
        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setProductName("Item 1");
        item.setQuantity(2);
        item.setUnitPrice(BigDecimal.TEN);
        item.setSubtotal(BigDecimal.valueOf(20));
        order.addItem(item);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        List<OrderItemResponse> items = orderItemService.findAllByOrderId(1L);

        assertEquals(1, items.size());
        assertEquals("Item 1", items.get(0).getProductName());
    }

    @Test
    void findAllByOrderId_OrderNotFound_ShouldThrowResourceNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderItemService.findAllByOrderId(99L));
    }

    @Test
    void addItemToOrder_ShouldAddItemAndRecalculateTotal() {
        OrderItemRequest request = new OrderItemRequest();
        request.setProductName("Novo Item");
        request.setQuantity(3);
        request.setUnitPrice(BigDecimal.valueOf(100));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderItemResponse response = orderItemService.addItemToOrder(1L, request);

        assertEquals("Novo Item", response.getProductName());
        assertEquals(BigDecimal.valueOf(300), response.getSubtotal());

        verify(orderRepository, times(1)).save(order);
        assertEquals(BigDecimal.valueOf(300), order.getTotalAmount()); // total atualizado
    }

    @Test
    void addItemToOrder_OrderNotPending_ShouldThrowBusinessException() {
        order.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderItemRequest request = new OrderItemRequest();
        request.setProductName("Item");
        request.setQuantity(1);
        request.setUnitPrice(BigDecimal.TEN);

        assertThrows(BusinessException.class, () -> orderItemService.addItemToOrder(1L, request));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void addItemToOrder_OrderNotFound_ShouldThrowResourceNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        OrderItemRequest request = new OrderItemRequest();
        request.setProductName("Item");
        request.setQuantity(1);
        request.setUnitPrice(BigDecimal.TEN);

        assertThrows(ResourceNotFoundException.class, () -> orderItemService.addItemToOrder(99L, request));
    }
}
