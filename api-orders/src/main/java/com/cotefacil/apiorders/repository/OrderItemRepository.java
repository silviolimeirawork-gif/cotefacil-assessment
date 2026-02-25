package com.cotefacil.apiorders.repository;

import com.cotefacil.apiorders.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
