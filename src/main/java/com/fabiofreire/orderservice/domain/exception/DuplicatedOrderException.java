package com.fabiofreire.orderservice.domain.exception;

public class DuplicatedOrderException extends RuntimeException {

    public DuplicatedOrderException(String externalOrderId) {
        super("Order already exists: " + externalOrderId);
    }
}
