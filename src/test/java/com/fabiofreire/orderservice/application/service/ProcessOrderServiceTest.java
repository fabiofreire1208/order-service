package com.fabiofreire.orderservice.application.service;

import com.fabiofreire.orderservice.application.port.out.OrderRepositoryPort;
import com.fabiofreire.orderservice.domain.exception.OrderNotFoundException;
import com.fabiofreire.orderservice.domain.model.Order;
import com.fabiofreire.orderservice.domain.model.OrderItem;
import com.fabiofreire.orderservice.domain.model.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessOrderServiceTest {

    @Mock
    private OrderRepositoryPort orderRepository;

    @InjectMocks
    private ProcessOrderService processOrderService;

    @Test
    void shouldCalculateOrderAndUpdateStatus() {
        Order order = Order.create("ext-001", "customer-1",
                List.of(OrderItem.create("product-1", 2, new BigDecimal("15.00"))));
        UUID orderId = order.getId();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        processOrderService.execute(orderId);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CALCULATED);
        assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("30.00"));
        verify(orderRepository).save(order);
    }

    @Test
    void shouldIgnoreAlreadyProcessedOrder() {
        Order order = Order.create("ext-001", "customer-1",
                List.of(OrderItem.create("product-1", 1, new BigDecimal("10.00"))));
        order.markAsProcessing();
        order.calculateTotal();
        order.markAsCalculated();
        UUID orderId = order.getId();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        processOrderService.execute(orderId);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenOrderNotFound() {
        UUID unknownId = UUID.randomUUID();
        when(orderRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> processOrderService.execute(unknownId))
                .isInstanceOf(OrderNotFoundException.class);
    }
}
