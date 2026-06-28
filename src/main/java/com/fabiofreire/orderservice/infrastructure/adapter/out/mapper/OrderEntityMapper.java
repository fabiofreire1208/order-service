package com.fabiofreire.orderservice.infrastructure.adapter.out.mapper;

import com.fabiofreire.orderservice.domain.model.Order;
import com.fabiofreire.orderservice.domain.model.OrderItem;
import com.fabiofreire.orderservice.infrastructure.adapter.out.entity.OrderEntity;
import com.fabiofreire.orderservice.infrastructure.adapter.out.entity.OrderItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderEntityMapper {

    @Mapping(target = "order", ignore = true)
    OrderItemEntity toEntity(OrderItem item);

    @Mapping(target = "items", ignore = true)
    OrderEntity toEntity(Order order);

    default Order toDomain(OrderEntity entity) {
        List<OrderItem> items = entity.getItems().stream()
                .map(this::toDomain)
                .toList();
        return Order.reconstitute(
                entity.getId(),
                entity.getExternalOrderId(),
                entity.getCustomerId(),
                entity.getStatus(),
                entity.getTotalAmount(),
                items,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    default OrderItem toDomain(OrderItemEntity entity) {
        return OrderItem.reconstitute(
                entity.getId(),
                entity.getProductId(),
                entity.getQuantity(),
                entity.getUnitPrice()
        );
    }
}
