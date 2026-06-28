package com.fabiofreire.orderservice.domain.model;

import com.fabiofreire.orderservice.domain.exception.InvalidOrderException;
import com.fabiofreire.orderservice.domain.exception.InvalidOrderStateException;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
public class Order {

    private final UUID id;
    private final String externalOrderId;
    private final String customerId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private final List<OrderItem> items;
    private final Instant createdAt;
    private Instant updatedAt;

    private Order(UUID id, String externalOrderId, String customerId, OrderStatus status,
                  BigDecimal totalAmount, List<OrderItem> items, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.externalOrderId = externalOrderId;
        this.customerId = customerId;
        this.status = status;
        this.totalAmount = totalAmount;
        this.items = List.copyOf(items);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Order create(String externalOrderId, String customerId, List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new InvalidOrderException("Order must contain at least one item");
        }
        Instant now = Instant.now();
        return new Order(UUID.randomUUID(), externalOrderId, customerId,
                OrderStatus.RECEIVED, BigDecimal.ZERO, items, now, now);
    }

    public static Order reconstitute(UUID id, String externalOrderId, String customerId,
                                     OrderStatus status, BigDecimal totalAmount,
                                     List<OrderItem> items, Instant createdAt, Instant updatedAt) {
        return new Order(id, externalOrderId, customerId, status, totalAmount, items, createdAt, updatedAt);
    }

    public void calculateTotal() {
        this.totalAmount = items.stream()
                .map(OrderItem::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.updatedAt = Instant.now();
    }

    public void markAsProcessing() {
        if (this.status != OrderStatus.RECEIVED) {
            throw new InvalidOrderStateException(
                    "Order must be in RECEIVED status to start processing, current status: " + this.status);
        }
        this.status = OrderStatus.PROCESSING;
        this.updatedAt = Instant.now();
    }

    public void markAsCalculated() {
        if (this.status != OrderStatus.PROCESSING) {
            throw new InvalidOrderStateException(
                    "Order must be in PROCESSING status to be calculated, current status: " + this.status);
        }
        this.status = OrderStatus.CALCULATED;
        this.updatedAt = Instant.now();
    }

    public void markAsFailed() {
        if (this.status != OrderStatus.PROCESSING) {
            throw new InvalidOrderStateException(
                    "Order must be in PROCESSING status to be marked as failed, current status: " + this.status);
        }
        this.status = OrderStatus.FAILED;
        this.updatedAt = Instant.now();
    }
}
