package com.cotefacil.apiorders.controller;

import com.cotefacil.apiorders.dto.OrderRequest;
import com.cotefacil.apiorders.dto.OrderResponse;
import com.cotefacil.apiorders.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> findAll(
            @PageableDefault(page = 0, size = 10, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Usuário {} listando pedidos", username);
        return ResponseEntity.ok(orderService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> findById(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Usuário {} buscando pedido ID: {}", username, id);        
        return ResponseEntity.ok(orderService.findById(id));
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Usuário {} criando novo pedido", username);        
        return new ResponseEntity<>(orderService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> update(@PathVariable Long id, @Valid @RequestBody OrderRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Usuário {} atualizando pedido ID: {}", username, id);        
        return ResponseEntity.ok(orderService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Usuário {} deletando pedido ID: {}", username, id);        
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
