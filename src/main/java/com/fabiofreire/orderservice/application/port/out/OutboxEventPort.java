package com.fabiofreire.orderservice.application.port.out;

import java.util.UUID;

public interface OutboxEventPort {

    void saveOrderReceivedEvent(UUID orderId, String externalOrderId);
}
