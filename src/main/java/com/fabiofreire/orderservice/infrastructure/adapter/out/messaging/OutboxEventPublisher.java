package com.fabiofreire.orderservice.infrastructure.adapter.out.messaging;

import com.fabiofreire.orderservice.infrastructure.adapter.out.entity.OutboxEventEntity;
import com.fabiofreire.orderservice.infrastructure.adapter.out.repository.OutboxEventJpaRepository;
import com.fabiofreire.orderservice.infrastructure.config.RabbitMQConfig;
import com.fabiofreire.orderservice.shared.OrderReceivedMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_PUBLISHED = "PUBLISHED";

    private final OutboxEventJpaRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEventEntity> pendingEvents = outboxEventRepository.findByStatus(STATUS_PENDING);
        for (OutboxEventEntity event : pendingEvents) {
            try {
                OrderReceivedMessage message = objectMapper.readValue(event.getPayload(), OrderReceivedMessage.class);
                rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, message);
                event.setStatus(STATUS_PUBLISHED);
                event.setPublishedAt(Instant.now());
                outboxEventRepository.save(event);
                log.debug("Published outbox event: {}", event.getId());
            } catch (JsonProcessingException e) {
                log.error("Failed to parse outbox event payload: {}", event.getId(), e);
            }
        }
    }
}
