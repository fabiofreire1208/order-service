package com.fabiofreire.orderservice.application.port.in;

import java.util.UUID;

public interface ProcessOrderUseCase {

    void execute(UUID orderId);
}
