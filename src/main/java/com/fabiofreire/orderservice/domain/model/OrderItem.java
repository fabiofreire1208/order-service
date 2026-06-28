package com.fabiofreire.orderservice.domain.model;

import com.fabiofreire.orderservice.domain.exception.InvalidOrderException;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class OrderItem {

    private final UUID id;
    private final String productId;
    private final Integer quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal totalAmount;

    private OrderItem(UUID id, String productId, Integer quantity, BigDecimal unitPrice) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public static OrderItem create(String productId, Integer quantity, BigDecimal unitPrice) {
        if (quantity == null || quantity <= 0) {
            throw new InvalidOrderException("Quantity must be greater than zero");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidOrderException("Unit price cannot be negative");
        }
        return new OrderItem(UUID.randomUUID(), productId, quantity, unitPrice);
    }

    public static OrderItem reconstitute(UUID id, String productId, Integer quantity, BigDecimal unitPrice) {
        return new OrderItem(id, productId, quantity, unitPrice);
    }
}
