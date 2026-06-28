package com.fabiofreire.orderservice.infrastructure.adapter.out;

import com.fabiofreire.orderservice.application.port.out.OutboxEventPort;
import com.fabiofreire.orderservice.infrastructure.adapter.out.entity.OutboxEventEntity;
import com.fabiofreire.orderservice.infrastructure.adapter.out.repository.OutboxEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OutboxEventPersistenceAdapter implements OutboxEventPort {

    private static final String EVENT_TYPE = "ORDER_RECEIVED";
    private static final String STATUS_PENDING = "PENDING";

    private final OutboxEventJpaRepository outboxEventJpaRepository;

    @Override
    public void saveOrderReceivedEvent(UUID orderId, String externalOrderId) {
        String payload = String.format(
                "{\"orderId\":\"%s\",\"externalOrderId\":\"%s\"}", orderId, externalOrderId);

        OutboxEventEntity entity = OutboxEventEntity.builder()
                .id(UUID.randomUUID())
                .aggregateId(orderId)
                .eventType(EVENT_TYPE)
                .payload(payload)
                .status(STATUS_PENDING)
                .createdAt(Instant.now())
                .build();

        outboxEventJpaRepository.save(entity);
    }
}
