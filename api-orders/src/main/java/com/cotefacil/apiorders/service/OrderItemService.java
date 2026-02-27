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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class OrderItemService {

    private static final Logger log = LoggerFactory.getLogger(OrderItemService.class);

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;

    public OrderItemService(OrderItemRepository orderItemRepository, OrderRepository orderRepository) {
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
    }

    public List<OrderItemResponse> findAllByOrderId(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return order.getItems().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public OrderItemResponse addItemToOrder(Long orderId, OrderItemRequest request) {
        log.debug("Adicionando item ao pedido {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Cannot add items to order with status " + order.getStatus());
        }

        OrderItem item = new OrderItem();
        item.setProductName(request.getProductName());
        item.setQuantity(request.getQuantity());
        item.setUnitPrice(request.getUnitPrice());
        item.calculateSubtotal();
        order.addItem(item);
        order.recalculateTotal();
        orderRepository.save(order);
        log.info("Item adicionado ao pedido {}. Produto: {}, quantidade: {}", orderId, request.getProductName(), request.getQuantity());        
        return toResponse(item);
    }

    private OrderItemResponse toResponse(OrderItem item) {
        OrderItemResponse resp = new OrderItemResponse();
        resp.setId(item.getId());
        resp.setProductName(item.getProductName());
        resp.setQuantity(item.getQuantity());
        resp.setUnitPrice(item.getUnitPrice());
        resp.setSubtotal(item.getSubtotal());
        return resp;
    }
}
