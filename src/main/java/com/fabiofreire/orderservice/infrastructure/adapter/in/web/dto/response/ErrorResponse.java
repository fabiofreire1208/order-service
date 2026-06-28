package com.fabiofreire.orderservice.infrastructure.adapter.in.web.dto.response;

import java.time.Instant;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String message,
        String path
) {}
