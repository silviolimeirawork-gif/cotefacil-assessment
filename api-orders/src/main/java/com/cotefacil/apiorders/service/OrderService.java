package com.cotefacil.apiorders.service;

import com.cotefacil.apiorders.dto.OrderRequest;
import com.cotefacil.apiorders.dto.OrderResponse;
import com.cotefacil.apiorders.exception.BusinessException;
import com.cotefacil.apiorders.exception.ResourceNotFoundException;
import com.cotefacil.apiorders.model.Order;
import com.cotefacil.apiorders.model.OrderItem;
import com.cotefacil.apiorders.model.OrderStatus;
import com.cotefacil.apiorders.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.stream.Collectors;
import com.cotefacil.apiorders.dto.OrderItemResponse;
import com.cotefacil.apiorders.exception.BusinessException;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemService orderItemService;

    public OrderService(OrderRepository orderRepository, OrderItemService orderItemService) {
        this.orderRepository = orderRepository;
        this.orderItemService = orderItemService;
    }

    public Page<OrderResponse> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::toResponse);
    }

    public OrderResponse findById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return toResponse(order);
    }

    @Transactional
    public OrderResponse create(OrderRequest request) {
        Order order = new Order();
        order.setCustomerName(request.getCustomerName());
        order.setCustomerEmail(request.getCustomerEmail());
        // salvar primeiro para gerar ID
        Order savedOrder = orderRepository.save(order);

        if (request.getItems() != null) {
            request.getItems().forEach(itemRequest -> {
                OrderItem item = new OrderItem();
                item.setProductName(itemRequest.getProductName());
                item.setQuantity(itemRequest.getQuantity());
                item.setUnitPrice(itemRequest.getUnitPrice());
                item.calculateSubtotal(); // chama @PreUpdate manualmente? Melhor usar set
                savedOrder.addItem(item);
            });
        }
        savedOrder.recalculateTotal();
        return toResponse(orderRepository.save(savedOrder));
    }

    @Transactional
    public OrderResponse update(Long id, OrderRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        // Não permitir alteração se cancelado ou entregue? Regra de negócio
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new BusinessException("Cannot update order with status " + order.getStatus());
        }

        order.setCustomerName(request.getCustomerName());
        order.setCustomerEmail(request.getCustomerEmail());

        // Remover itens antigos e adicionar novos? Ou atualizar? Vamos substituir a lista
        order.getItems().clear();
        if (request.getItems() != null) {
            request.getItems().forEach(itemRequest -> {
                OrderItem item = new OrderItem();
                item.setProductName(itemRequest.getProductName());
                item.setQuantity(itemRequest.getQuantity());
                item.setUnitPrice(itemRequest.getUnitPrice());
                item.calculateSubtotal();
                order.addItem(item);
            });
        }
        order.recalculateTotal();
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public void delete(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Only pending orders can be deleted");
        }
        orderRepository.delete(order);
    }

    private OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setCustomerName(order.getCustomerName());
        response.setCustomerEmail(order.getCustomerEmail());
        response.setOrderDate(order.getOrderDate());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setItems(order.getItems().stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList()));
        return response;
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        OrderItemResponse resp = new OrderItemResponse();
        resp.setId(item.getId());
        resp.setProductName(item.getProductName());
        resp.setQuantity(item.getQuantity());
        resp.setUnitPrice(item.getUnitPrice());
        resp.setSubtotal(item.getSubtotal());
        return resp;
    }
}
