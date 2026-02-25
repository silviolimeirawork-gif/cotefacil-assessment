package com.cotefacil.apiorders.repository;

import com.cotefacil.apiorders.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
