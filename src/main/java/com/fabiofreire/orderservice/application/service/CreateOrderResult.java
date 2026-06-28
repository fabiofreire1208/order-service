package com.fabiofreire.orderservice.application.service;

import com.fabiofreire.orderservice.domain.model.Order;

public record CreateOrderResult(Order order, boolean created) {}
