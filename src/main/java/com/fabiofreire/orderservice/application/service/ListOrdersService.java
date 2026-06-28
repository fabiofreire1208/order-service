package com.fabiofreire.orderservice.application.service;

import com.fabiofreire.orderservice.application.port.in.ListOrdersUseCase;
import com.fabiofreire.orderservice.application.port.out.OrderRepositoryPort;
import com.fabiofreire.orderservice.domain.model.Order;
import com.fabiofreire.orderservice.domain.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListOrdersService implements ListOrdersUseCase {

    private final OrderRepositoryPort orderRepository;

    @Override
    public Page<Order> execute(OrderStatus status, String customerId, Pageable pageable) {
        return orderRepository.findAll(status, customerId, pageable);
    }
}
