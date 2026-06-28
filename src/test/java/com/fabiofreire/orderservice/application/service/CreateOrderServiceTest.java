package com.fabiofreire.orderservice.application.service;

import com.fabiofreire.orderservice.application.command.CreateOrderCommand;
import com.fabiofreire.orderservice.application.command.OrderItemCommand;
import com.fabiofreire.orderservice.application.port.in.CreateOrderResult;
import com.fabiofreire.orderservice.application.port.out.OrderRepositoryPort;
import com.fabiofreire.orderservice.application.port.out.OutboxEventPort;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateOrderServiceTest {

    @Mock
    private OrderRepositoryPort orderRepository;

    @Mock
    private OutboxEventPort outboxEventPort;

    @InjectMocks
    private CreateOrderService createOrderService;

    @Test
    void shouldCreateNewOrder() {
        CreateOrderCommand command = buildCommand("ext-001");
        Order savedOrder = buildOrder("ext-001");

        when(orderRepository.findByExternalOrderId("ext-001")).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        CreateOrderResult result = createOrderService.execute(command);

        assertThat(result.created()).isTrue();
        assertThat(result.order().getExternalOrderId()).isEqualTo("ext-001");
        assertThat(result.order().getStatus()).isEqualTo(OrderStatus.RECEIVED);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void shouldCreateOutboxEventOnNewOrder() {
        CreateOrderCommand command = buildCommand("ext-001");
        Order savedOrder = buildOrder("ext-001");

        when(orderRepository.findByExternalOrderId("ext-001")).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        createOrderService.execute(command);

        verify(outboxEventPort).saveOrderReceivedEvent(savedOrder.getId(), "ext-001");
    }

    @Test
    void shouldReturnExistingOrderWhenDuplicate() {
        CreateOrderCommand command = buildCommand("ext-001");
        Order existingOrder = buildOrder("ext-001");

        when(orderRepository.findByExternalOrderId("ext-001")).thenReturn(Optional.of(existingOrder));

        CreateOrderResult result = createOrderService.execute(command);

        assertThat(result.created()).isFalse();
        assertThat(result.order().getExternalOrderId()).isEqualTo("ext-001");
        verify(orderRepository, never()).save(any());
        verify(outboxEventPort, never()).saveOrderReceivedEvent(any(), anyString());
    }

    private CreateOrderCommand buildCommand(String externalOrderId) {
        return new CreateOrderCommand(
                externalOrderId,
                "customer-1",
                List.of(new OrderItemCommand("product-1", 2, new BigDecimal("10.00")))
        );
    }

    private Order buildOrder(String externalOrderId) {
        return Order.create(
                externalOrderId,
                "customer-1",
                List.of(OrderItem.create("product-1", 2, new BigDecimal("10.00")))
        );
    }
}
