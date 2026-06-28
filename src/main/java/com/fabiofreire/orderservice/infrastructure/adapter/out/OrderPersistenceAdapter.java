package com.fabiofreire.orderservice.infrastructure.adapter.out;

import com.fabiofreire.orderservice.application.port.out.OrderRepositoryPort;
import com.fabiofreire.orderservice.domain.model.Order;
import com.fabiofreire.orderservice.domain.model.OrderStatus;
import com.fabiofreire.orderservice.infrastructure.adapter.out.entity.OrderEntity;
import com.fabiofreire.orderservice.infrastructure.adapter.out.entity.OrderItemEntity;
import com.fabiofreire.orderservice.infrastructure.adapter.out.mapper.OrderEntityMapper;
import com.fabiofreire.orderservice.infrastructure.adapter.out.repository.OrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderPersistenceAdapter implements OrderRepositoryPort {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderEntityMapper mapper;

    @Override
    public Order save(Order order) {
        OrderEntity entity = mapper.toEntity(order);
        List<OrderItemEntity> items = order.getItems().stream()
                .map(mapper::toEntity)
                .peek(item -> item.setOrder(entity))
                .toList();
        entity.setItems(items);
        return mapper.toDomain(orderJpaRepository.save(entity));
    }

    @Override
    public Optional<Order> findByExternalOrderId(String externalOrderId) {
        return orderJpaRepository.findByExternalOrderId(externalOrderId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return orderJpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Page<Order> findAll(OrderStatus status, String customerId, Pageable pageable) {
        if (status != null && customerId != null) {
            return orderJpaRepository.findByStatusAndCustomerId(status, customerId, pageable)
                    .map(mapper::toDomain);
        }
        if (status != null) {
            return orderJpaRepository.findByStatus(status, pageable)
                    .map(mapper::toDomain);
        }
        if (customerId != null) {
            return orderJpaRepository.findByCustomerId(customerId, pageable)
                    .map(mapper::toDomain);
        }
        return orderJpaRepository.findAll(pageable)
                .map(mapper::toDomain);
    }
}
