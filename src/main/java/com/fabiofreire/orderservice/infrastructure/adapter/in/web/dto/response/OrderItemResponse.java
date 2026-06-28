package com.fabiofreire.orderservice.infrastructure.adapter.in.web.dto.response;

import java.math.BigDecimal;

public record OrderItemResponse(
        String productId,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalAmount
) {}
