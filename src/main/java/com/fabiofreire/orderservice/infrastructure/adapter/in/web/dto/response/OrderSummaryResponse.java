package com.fabiofreire.orderservice.infrastructure.adapter.in.web.dto.response;

import java.math.BigDecimal;

public record OrderSummaryResponse(
        String externalOrderId,
        String customerId,
        String status,
        BigDecimal totalAmount
) {}
