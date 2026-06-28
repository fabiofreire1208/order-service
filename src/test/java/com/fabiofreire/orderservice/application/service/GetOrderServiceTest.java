package com.fabiofreire.orderservice.application.service;

import com.fabiofreire.orderservice.application.port.out.OrderRepositoryPort;
import com.fabiofreire.orderservice.domain.exception.OrderNotFoundException;
import com.fabiofreire.orderservice.domain.model.Order;
import com.fabiofreire.orderservice.domain.model.OrderItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetOrderServiceTest {

    @Mock
    private OrderRepositoryPort orderRepository;

    @InjectMocks
    private GetOrderService getOrderService;

    @Test
    void shouldReturnExistingOrder() {
        Order order = Order.create("ext-001", "customer-1",
                List.of(OrderItem.create("product-1", 1, new BigDecimal("10.00"))));

        when(orderRepository.findByExternalOrderId("ext-001")).thenReturn(Optional.of(order));

        Order result = getOrderService.execute("ext-001");

        assertThat(result.getExternalOrderId()).isEqualTo("ext-001");
    }

    @Test
    void shouldThrowWhenOrderNotFound() {
        when(orderRepository.findByExternalOrderId("not-found")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getOrderService.execute("not-found"))
                .isInstanceOf(OrderNotFoundException.class);
    }
}
