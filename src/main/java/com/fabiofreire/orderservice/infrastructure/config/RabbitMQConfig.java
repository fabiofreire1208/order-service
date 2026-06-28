package com.fabiofreire.orderservice.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "order.exchange";
    public static final String QUEUE = "order.received.queue";
    public static final String DLQ = "order.received.dlq";
    public static final String ROUTING_KEY = "order.received";

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue orderReceivedDlq() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    public Queue orderReceivedQueue() {
        return QueueBuilder.durable(QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", DLQ)
                .build();
    }

    @Bean
    public Binding orderReceivedBinding(Queue orderReceivedQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(orderReceivedQueue).to(orderExchange).with(ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
