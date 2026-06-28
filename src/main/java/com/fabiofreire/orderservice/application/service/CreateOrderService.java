package com.fabiofreire.orderservice.application.service;

import com.fabiofreire.orderservice.application.command.CreateOrderCommand;
import com.fabiofreire.orderservice.application.port.in.CreateOrderUseCase;
import com.fabiofreire.orderservice.application.port.out.OrderRepositoryPort;
import com.fabiofreire.orderservice.application.port.out.OutboxEventPort;
import com.fabiofreire.orderservice.domain.model.Order;
import com.fabiofreire.orderservice.domain.model.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CreateOrderService implements CreateOrderUseCase {

    private final OrderRepositoryPort orderRepository;
    private final OutboxEventPort outboxEventPort;

    @Override
    public CreateOrderResult execute(CreateOrderCommand command) {
        Optional<Order> existing = orderRepository.findByExternalOrderId(command.externalOrderId());
        if (existing.isPresent()) {
            return new CreateOrderResult(existing.get(), false);
        }

        List<OrderItem> items = command.items().stream()
                .map(item -> OrderItem.create(item.productId(), item.quantity(), item.unitPrice()))
                .toList();

        Order order = Order.create(command.externalOrderId(), command.customerId(), items);
        Order saved = orderRepository.save(order);

        outboxEventPort.saveOrderReceivedEvent(saved.getId(), saved.getExternalOrderId());

        return new CreateOrderResult(saved, true);
    }
}
