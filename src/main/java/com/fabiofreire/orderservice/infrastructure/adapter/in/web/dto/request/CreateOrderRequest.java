package com.fabiofreire.orderservice.infrastructure.adapter.in.web.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(
        @NotBlank String externalOrderId,
        @NotBlank String customerId,
        @NotEmpty @Valid List<OrderItemRequest> items
) {}
