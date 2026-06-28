package com.fabiofreire.orderservice.infrastructure.adapter.in.web.dto.response;

public record CreateOrderResponse(
        String externalOrderId,
        String status,
        String message
) {}
