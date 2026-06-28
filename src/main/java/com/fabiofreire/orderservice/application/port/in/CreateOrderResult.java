package com.fabiofreire.orderservice.application.port.in;

import com.fabiofreire.orderservice.domain.model.Order;

public record CreateOrderResult(Order order, boolean created) {}
