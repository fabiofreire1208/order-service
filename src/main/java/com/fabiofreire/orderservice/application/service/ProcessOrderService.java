package com.fabiofreire.orderservice.application.service;

import com.fabiofreire.orderservice.application.port.in.ProcessOrderUseCase;
import com.fabiofreire.orderservice.application.port.out.OrderRepositoryPort;
import com.fabiofreire.orderservice.domain.model.Order;
import com.fabiofreire.orderservice.domain.model.OrderStatus;
import com.fabiofreire.orderservice.domain.exception.OrderNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProcessOrderService implements ProcessOrderUseCase {

    private final OrderRepositoryPort orderRepository;

    @Transactional
    @Override
    public void execute(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId.toString()));

        if (order.getStatus() != OrderStatus.RECEIVED) {
            return;
        }

        order.markAsProcessing();
        order.calculateTotal();
        order.markAsCalculated();

        orderRepository.save(order);
    }
}
