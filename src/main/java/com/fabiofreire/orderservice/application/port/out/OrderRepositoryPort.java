package com.fabiofreire.orderservice.application.port.out;

import com.fabiofreire.orderservice.domain.model.Order;
import com.fabiofreire.orderservice.domain.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepositoryPort {

    Order save(Order order);

    Optional<Order> findByExternalOrderId(String externalOrderId);

    Optional<Order> findById(UUID id);

    Page<Order> findAll(OrderStatus status, String customerId, Pageable pageable);
}
