package com.fabiofreire.orderservice.application.command;

import java.math.BigDecimal;

public record OrderItemCommand(
        String productId,
        Integer quantity,
        BigDecimal unitPrice
) {}
