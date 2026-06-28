package com.fabiofreire.orderservice.infrastructure.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record OrderItemRequest(
        @NotBlank String productId,
        @Positive Integer quantity,
        @PositiveOrZero BigDecimal unitPrice
) {}
