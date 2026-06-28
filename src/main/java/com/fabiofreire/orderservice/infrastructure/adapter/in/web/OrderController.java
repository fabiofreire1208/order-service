package com.fabiofreire.orderservice.infrastructure.adapter.in.web;

import com.fabiofreire.orderservice.application.port.in.CreateOrderUseCase;
import com.fabiofreire.orderservice.application.port.in.GetOrderUseCase;
import com.fabiofreire.orderservice.application.port.in.ListOrdersUseCase;
import com.fabiofreire.orderservice.application.port.in.CreateOrderResult;
import com.fabiofreire.orderservice.domain.model.Order;
import com.fabiofreire.orderservice.domain.model.OrderStatus;
import com.fabiofreire.orderservice.infrastructure.adapter.in.web.dto.request.CreateOrderRequest;
import com.fabiofreire.orderservice.infrastructure.adapter.in.web.dto.response.CreateOrderResponse;
import com.fabiofreire.orderservice.infrastructure.adapter.in.web.dto.response.OrderResponse;
import com.fabiofreire.orderservice.infrastructure.adapter.in.web.dto.response.PagedOrderResponse;
import com.fabiofreire.orderservice.infrastructure.adapter.in.web.mapper.OrderDtoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final ListOrdersUseCase listOrdersUseCase;
    private final OrderDtoMapper mapper;

    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        CreateOrderResult result = createOrderUseCase.execute(mapper.toCommand(request));

        CreateOrderResponse response = new CreateOrderResponse(
                result.order().getExternalOrderId(),
                result.order().getStatus().name(),
                result.created() ? "Order accepted for processing." : "Order already exists."
        );

        return result.created()
                ? ResponseEntity.accepted().body(response)
                : ResponseEntity.ok(response);
    }

    @GetMapping("/{externalOrderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String externalOrderId) {
        Order order = getOrderUseCase.execute(externalOrderId);
        return ResponseEntity.ok(mapper.toResponse(order));
    }

    @GetMapping
    public ResponseEntity<PagedOrderResponse> listOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        OrderStatus orderStatus = status != null ? OrderStatus.valueOf(status) : null;
        Page<Order> orders = listOrdersUseCase.execute(orderStatus, customerId, PageRequest.of(page, size));

        PagedOrderResponse response = new PagedOrderResponse(
                orders.getContent().stream().map(mapper::toSummaryResponse).toList(),
                orders.getNumber(),
                orders.getSize(),
                orders.getTotalElements(),
                orders.getTotalPages()
        );

        return ResponseEntity.ok(response);
    }
}
