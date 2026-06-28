package com.fabiofreire.orderservice.infrastructure.adapter.in.web.mapper;

import com.fabiofreire.orderservice.application.command.CreateOrderCommand;
import com.fabiofreire.orderservice.application.command.OrderItemCommand;
import com.fabiofreire.orderservice.domain.model.Order;
import com.fabiofreire.orderservice.domain.model.OrderItem;
import com.fabiofreire.orderservice.infrastructure.adapter.in.web.dto.request.CreateOrderRequest;
import com.fabiofreire.orderservice.infrastructure.adapter.in.web.dto.request.OrderItemRequest;
import com.fabiofreire.orderservice.infrastructure.adapter.in.web.dto.response.OrderItemResponse;
import com.fabiofreire.orderservice.infrastructure.adapter.in.web.dto.response.OrderResponse;
import com.fabiofreire.orderservice.infrastructure.adapter.in.web.dto.response.OrderSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderDtoMapper {

    OrderItemCommand toCommand(OrderItemRequest request);

    CreateOrderCommand toCommand(CreateOrderRequest request);

    OrderItemResponse toResponse(OrderItem item);

    @Mapping(target = "status", expression = "java(order.getStatus().name())")
    OrderResponse toResponse(Order order);

    @Mapping(target = "status", expression = "java(order.getStatus().name())")
    OrderSummaryResponse toSummaryResponse(Order order);
}
