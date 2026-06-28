package com.fabiofreire.orderservice.application.port.in;

import com.fabiofreire.orderservice.domain.model.Order;
import com.fabiofreire.orderservice.domain.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ListOrdersUseCase {

    Page<Order> execute(OrderStatus status, String customerId, Pageable pageable);
}
