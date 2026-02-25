package com.cotefacil.apiorders.controller;

import com.cotefacil.apiorders.dto.OrderItemRequest;
import com.cotefacil.apiorders.dto.OrderItemResponse;
import com.cotefacil.apiorders.service.OrderItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders/{orderId}/items")
public class OrderItemController {

    private final OrderItemService orderItemService;

    public OrderItemController(OrderItemService orderItemService) {
        this.orderItemService = orderItemService;
    }

    @GetMapping
    public ResponseEntity<List<OrderItemResponse>> findAll(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderItemService.findAllByOrderId(orderId));
    }

    @PostMapping
    public ResponseEntity<OrderItemResponse> addItem(@PathVariable Long orderId, @Valid @RequestBody OrderItemRequest request) {
        return new ResponseEntity<>(orderItemService.addItemToOrder(orderId, request), HttpStatus.CREATED);
    }
}
