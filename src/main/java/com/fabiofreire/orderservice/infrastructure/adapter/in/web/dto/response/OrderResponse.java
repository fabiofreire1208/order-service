package com.fabiofreire.orderservice.infrastructure.adapter.in.web.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        String externalOrderId,
        String customerId,
        String status,
        BigDecimal totalAmount,
        List<OrderItemResponse> items,
        Instant createdAt,
        Instant updatedAt
) {}
