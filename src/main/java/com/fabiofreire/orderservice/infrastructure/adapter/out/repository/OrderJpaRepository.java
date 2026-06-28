package com.fabiofreire.orderservice.infrastructure.adapter.out.repository;

import com.fabiofreire.orderservice.domain.model.OrderStatus;
import com.fabiofreire.orderservice.infrastructure.adapter.out.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {

    Optional<OrderEntity> findByExternalOrderId(String externalOrderId);

    Page<OrderEntity> findByStatus(OrderStatus status, Pageable pageable);

    Page<OrderEntity> findByCustomerId(String customerId, Pageable pageable);

    Page<OrderEntity> findByStatusAndCustomerId(OrderStatus status, String customerId, Pageable pageable);
}
