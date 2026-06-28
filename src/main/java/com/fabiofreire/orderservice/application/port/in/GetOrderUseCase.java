package com.fabiofreire.orderservice.application.port.in;

import com.fabiofreire.orderservice.domain.model.Order;

public interface GetOrderUseCase {

    Order execute(String externalOrderId);
}
