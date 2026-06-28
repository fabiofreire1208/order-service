package com.fabiofreire.orderservice.application.service;

import com.fabiofreire.orderservice.application.port.in.GetOrderUseCase;
import com.fabiofreire.orderservice.application.port.out.OrderRepositoryPort;
import com.fabiofreire.orderservice.domain.exception.OrderNotFoundException;
import com.fabiofreire.orderservice.domain.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetOrderService implements GetOrderUseCase {

    private final OrderRepositoryPort orderRepository;

    @Override
    public Order execute(String externalOrderId) {
        return orderRepository.findByExternalOrderId(externalOrderId)
                .orElseThrow(() -> new OrderNotFoundException(externalOrderId));
    }
}
