package com.fabiofreire.orderservice.domain.exception;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(String externalOrderId) {
        super("Order not found: " + externalOrderId);
    }
}
