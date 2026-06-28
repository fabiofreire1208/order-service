package com.fabiofreire.orderservice.infrastructure.adapter.in.messaging;

import com.fabiofreire.orderservice.application.port.in.ProcessOrderUseCase;
import com.fabiofreire.orderservice.infrastructure.config.RabbitMQConfig;
import com.fabiofreire.orderservice.shared.OrderReceivedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderReceivedConsumer {

    private final ProcessOrderUseCase processOrderUseCase;

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void consume(OrderReceivedMessage message) {
        log.debug("Received order event: {}", message.externalOrderId());
        processOrderUseCase.execute(UUID.fromString(message.orderId()));
    }
}
