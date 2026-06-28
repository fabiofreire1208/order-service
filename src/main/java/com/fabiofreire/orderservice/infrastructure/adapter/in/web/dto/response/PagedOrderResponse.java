package com.fabiofreire.orderservice.infrastructure.adapter.in.web.dto.response;

import java.util.List;

public record PagedOrderResponse(
        List<OrderSummaryResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
