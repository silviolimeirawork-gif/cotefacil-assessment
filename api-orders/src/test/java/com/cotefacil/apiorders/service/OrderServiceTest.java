package com.cotefacil.apiorders.service;

import com.cotefacil.apiorders.dto.OrderItemRequest;
import com.cotefacil.apiorders.dto.OrderRequest;
import com.cotefacil.apiorders.dto.OrderResponse;
import com.cotefacil.apiorders.exception.BusinessException;
import com.cotefacil.apiorders.exception.ResourceNotFoundException;
import com.cotefacil.apiorders.model.Order;
import com.cotefacil.apiorders.model.OrderItem;
import com.cotefacil.apiorders.model.OrderStatus;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemService orderItemService;

    @InjectMocks
    private OrderService orderService;

    private Order order;
    private OrderItem item;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(1L);
        order.setCustomerName("João Silva");
        order.setCustomerEmail("joao@email.com");
        order.setStatus(OrderStatus.PENDING);

        item = new OrderItem();
        item.setId(1L);
        item.setProductName("Produto Teste");
        item.setQuantity(2);
        item.setUnitPrice(BigDecimal.TEN);
        item.setSubtotal(BigDecimal.valueOf(20));
        order.addItem(item);
        order.recalculateTotal(); // total = 20
    }

    @Test
    void findAll_ShouldReturnPagedOrders() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Order> page = new PageImpl<>(List.of(order));
        when(orderRepository.findAll(pageRequest)).thenReturn(page);

        Page<OrderResponse> result = orderService.findAll(pageRequest);

        assertEquals(1, result.getTotalElements());
        OrderResponse response = result.getContent().get(0);
        assertEquals("João Silva", response.getCustomerName());
        assertEquals(1, response.getItems().size());
    }

    @Test
    void findById_ExistingId_ShouldReturnOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.findById(1L);

        assertEquals(1L, response.getId());
        assertEquals("João Silva", response.getCustomerName());
        assertEquals(BigDecimal.valueOf(20), response.getTotalAmount());
    }

    @Test
    void findById_NonExistingId_ShouldThrowResourceNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.findById(99L));
    }

    @Test
    void create_ShouldSaveOrderWithoutItems() {
        OrderRequest request = new OrderRequest();
        request.setCustomerName("Maria");
        request.setCustomerEmail("maria@email.com");

        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(2L);
            return o;
        });

        OrderResponse response = orderService.create(request);

        assertEquals(2L, response.getId());
        assertEquals("Maria", response.getCustomerName());
        assertEquals(BigDecimal.ZERO, response.getTotalAmount());
        assertTrue(response.getItems().isEmpty());
    }

    @Test
    void create_ShouldSaveOrderWithItems() {
        OrderRequest request = new OrderRequest();
        request.setCustomerName("Carlos");
        request.setCustomerEmail("carlos@email.com");

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductName("Item 1");
        itemRequest.setQuantity(3);
        itemRequest.setUnitPrice(BigDecimal.valueOf(15));
        request.setItems(List.of(itemRequest));

        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            if (o.getId() == null) {
                o.setId(3L);
            }
            return o;
        });

        OrderResponse response = orderService.create(request);

        assertEquals(3L, response.getId());
        assertEquals("Carlos", response.getCustomerName());
        assertEquals(BigDecimal.valueOf(45), response.getTotalAmount());
        assertEquals(1, response.getItems().size());
    }

    @Test
    void update_ExistingPendingOrder_ShouldUpdate() {
        OrderRequest request = new OrderRequest();
        request.setCustomerName("João Souza");
        request.setCustomerEmail("joao.souza@email.com");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponse response = orderService.update(1L, request);

        assertEquals("João Souza", response.getCustomerName());
        assertEquals("joao.souza@email.com", response.getCustomerEmail());
    }

    @Test
    void update_OrderCancelled_ShouldThrowBusinessException() {
        order.setStatus(OrderStatus.CANCELLED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderRequest request = new OrderRequest();
        request.setCustomerName("Qualquer");
        request.setCustomerEmail("qualquer@email.com");

        assertThrows(BusinessException.class, () -> orderService.update(1L, request));
    }

    @Test
    void delete_ExistingPendingOrder_ShouldDelete() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        doNothing().when(orderRepository).delete(order);

        assertDoesNotThrow(() -> orderService.delete(1L));
        verify(orderRepository, times(1)).delete(order);
    }

    @Test
    void delete_OrderNotPending_ShouldThrowBusinessException() {
        order.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(BusinessException.class, () -> orderService.delete(1L));
        verify(orderRepository, never()).delete(any());
    }

    @Test
    void delete_NonExistingOrder_ShouldThrowResourceNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.delete(99L));
    }
}
