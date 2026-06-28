package com.fabiofreire.orderservice.application.port.in;

import com.fabiofreire.orderservice.application.command.CreateOrderCommand;

public interface CreateOrderUseCase {

    CreateOrderResult execute(CreateOrderCommand command);
}
