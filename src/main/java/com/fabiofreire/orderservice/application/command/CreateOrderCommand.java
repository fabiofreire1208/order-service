package com.fabiofreire.orderservice.application.command;

import java.util.List;

public record CreateOrderCommand(
        String externalOrderId,
        String customerId,
        List<OrderItemCommand> items
) {}
