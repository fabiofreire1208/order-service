package com.fabiofreire.orderservice;

import com.fabiofreire.orderservice.application.port.out.OrderRepositoryPort;
import com.fabiofreire.orderservice.application.port.out.OutboxEventPort;
import com.fabiofreire.orderservice.infrastructure.adapter.out.repository.OutboxEventJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class OrderServiceApplicationTests {

    @MockBean
    private OrderRepositoryPort orderRepositoryPort;

    @MockBean
    private OutboxEventPort outboxEventPort;

    @MockBean
    private OutboxEventJpaRepository outboxEventJpaRepository;

    @MockBean
    private ConnectionFactory connectionFactory;

    @Test
    void contextLoads() {
    }
}
