package com.fabiofreire.orderservice.shared;

public record OrderReceivedMessage(String orderId, String externalOrderId) {}
