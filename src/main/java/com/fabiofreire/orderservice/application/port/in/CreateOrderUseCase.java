package com.fabiofreire.orderservice.application.port.in;

import com.fabiofreire.orderservice.application.command.CreateOrderCommand;
import com.fabiofreire.orderservice.application.service.CreateOrderResult;

public interface CreateOrderUseCase {

    CreateOrderResult execute(CreateOrderCommand command);
}
