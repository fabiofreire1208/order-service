package com.fabiofreire.orderservice.domain.model;

import com.fabiofreire.orderservice.domain.exception.InvalidOrderException;
import com.fabiofreire.orderservice.domain.exception.InvalidOrderStateException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    @Test
    void shouldCreateOrderSuccessfully() {
        List<OrderItem> items = List.of(OrderItem.create("product-1", 2, new BigDecimal("10.00")));

        Order order = Order.create("ext-001", "customer-1", items);

        assertThat(order.getId()).isNotNull();
        assertThat(order.getExternalOrderId()).isEqualTo("ext-001");
        assertThat(order.getCustomerId()).isEqualTo("customer-1");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.RECEIVED);
        assertThat(order.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getCreatedAt()).isNotNull();
        assertThat(order.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldRejectOrderWithoutItems() {
        assertThatThrownBy(() -> Order.create("ext-001", "customer-1", Collections.emptyList()))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessageContaining("at least one item");
    }

    @Test
    void shouldRejectOrderWithNullItems() {
        assertThatThrownBy(() -> Order.create("ext-001", "customer-1", null))
                .isInstanceOf(InvalidOrderException.class);
    }

    @Test
    void shouldCalculateItemTotal() {
        OrderItem item = OrderItem.create("product-1", 3, new BigDecimal("15.50"));

        assertThat(item.getTotalAmount()).isEqualByComparingTo(new BigDecimal("46.50"));
    }

    @Test
    void shouldCalculateOrderTotal() {
        List<OrderItem> items = List.of(
                OrderItem.create("product-1", 2, new BigDecimal("10.00")),
                OrderItem.create("product-2", 1, new BigDecimal("25.00"))
        );
        Order order = Order.create("ext-001", "customer-1", items);

        order.calculateTotal();

        assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("45.00"));
    }

    @Test
    void shouldRejectZeroQuantity() {
        assertThatThrownBy(() -> OrderItem.create("product-1", 0, new BigDecimal("10.00")))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessageContaining("greater than zero");
    }

    @Test
    void shouldRejectNegativeQuantity() {
        assertThatThrownBy(() -> OrderItem.create("product-1", -1, new BigDecimal("10.00")))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessageContaining("greater than zero");
    }

    @Test
    void shouldRejectNegativeUnitPrice() {
        assertThatThrownBy(() -> OrderItem.create("product-1", 1, new BigDecimal("-0.01")))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessageContaining("cannot be negative");
    }

    @Test
    void shouldAllowZeroUnitPrice() {
        OrderItem item = OrderItem.create("product-1", 1, BigDecimal.ZERO);

        assertThat(item.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldMarkOrderAsProcessing() {
        Order order = Order.create("ext-001", "customer-1",
                List.of(OrderItem.create("product-1", 1, new BigDecimal("10.00"))));

        order.markAsProcessing();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PROCESSING);
    }

    @Test
    void shouldMarkOrderAsCalculated() {
        Order order = Order.create("ext-001", "customer-1",
                List.of(OrderItem.create("product-1", 1, new BigDecimal("10.00"))));
        order.markAsProcessing();

        order.markAsCalculated();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CALCULATED);
    }

    @Test
    void shouldMarkOrderAsFailed() {
        Order order = Order.create("ext-001", "customer-1",
                List.of(OrderItem.create("product-1", 1, new BigDecimal("10.00"))));
        order.markAsProcessing();

        order.markAsFailed();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.FAILED);
    }

    @Test
    void shouldRejectInvalidTransitionFromReceivedToCalculated() {
        Order order = Order.create("ext-001", "customer-1",
                List.of(OrderItem.create("product-1", 1, new BigDecimal("10.00"))));

        assertThatThrownBy(order::markAsCalculated)
                .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    void shouldRejectInvalidTransitionFromCalculatedToProcessing() {
        Order order = Order.create("ext-001", "customer-1",
                List.of(OrderItem.create("product-1", 1, new BigDecimal("10.00"))));
        order.markAsProcessing();
        order.markAsCalculated();

        assertThatThrownBy(order::markAsProcessing)
                .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    void shouldRejectInvalidTransitionFromReceivedToFailed() {
        Order order = Order.create("ext-001", "customer-1",
                List.of(OrderItem.create("product-1", 1, new BigDecimal("10.00"))));

        assertThatThrownBy(order::markAsFailed)
                .isInstanceOf(InvalidOrderStateException.class);
    }
}
