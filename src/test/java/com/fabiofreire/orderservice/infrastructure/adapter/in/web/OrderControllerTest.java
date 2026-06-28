package com.fabiofreire.orderservice.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fabiofreire.orderservice.application.port.in.CreateOrderUseCase;
import com.fabiofreire.orderservice.application.port.in.GetOrderUseCase;
import com.fabiofreire.orderservice.application.port.in.ListOrdersUseCase;
import com.fabiofreire.orderservice.application.service.CreateOrderResult;
import com.fabiofreire.orderservice.domain.exception.OrderNotFoundException;
import com.fabiofreire.orderservice.domain.model.Order;
import com.fabiofreire.orderservice.domain.model.OrderItem;
import com.fabiofreire.orderservice.domain.model.OrderStatus;
import com.fabiofreire.orderservice.infrastructure.adapter.in.web.dto.request.CreateOrderRequest;
import com.fabiofreire.orderservice.infrastructure.adapter.in.web.dto.request.OrderItemRequest;
import com.fabiofreire.orderservice.infrastructure.adapter.in.web.dto.response.OrderResponse;
import com.fabiofreire.orderservice.infrastructure.adapter.in.web.dto.response.OrderSummaryResponse;
import com.fabiofreire.orderservice.infrastructure.adapter.in.web.mapper.OrderDtoMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateOrderUseCase createOrderUseCase;

    @MockBean
    private GetOrderUseCase getOrderUseCase;

    @MockBean
    private ListOrdersUseCase listOrdersUseCase;

    @MockBean
    private OrderDtoMapper mapper;

    @Test
    void shouldReturn202WhenOrderCreated() throws Exception {
        Order order = buildOrder();
        CreateOrderRequest request = buildCreateRequest();

        when(mapper.toCommand(any(CreateOrderRequest.class))).thenReturn(null);
        when(createOrderUseCase.execute(any())).thenReturn(new CreateOrderResult(order, true));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.externalOrderId").value("ext-001"))
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andExpect(jsonPath("$.message").value("Order accepted for processing."));
    }

    @Test
    void shouldReturn200WhenOrderAlreadyExists() throws Exception {
        Order order = buildOrder();
        CreateOrderRequest request = buildCreateRequest();

        when(mapper.toCommand(any(CreateOrderRequest.class))).thenReturn(null);
        when(createOrderUseCase.execute(any())).thenReturn(new CreateOrderResult(order, false));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order already exists."));
    }

    @Test
    void shouldReturn400WhenRequestBodyIsInvalid() throws Exception {
        CreateOrderRequest invalidRequest = new CreateOrderRequest("", "customer-1",
                List.of(new OrderItemRequest("p1", 1, BigDecimal.TEN)));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn400WhenItemsAreEmpty() throws Exception {
        CreateOrderRequest invalidRequest = new CreateOrderRequest("ext-001", "customer-1", List.of());

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn200WhenOrderFound() throws Exception {
        Order order = buildOrder();
        OrderResponse response = new OrderResponse("ext-001", "customer-1", "RECEIVED",
                BigDecimal.ZERO, List.of(), Instant.now(), Instant.now());

        when(getOrderUseCase.execute("ext-001")).thenReturn(order);
        when(mapper.toResponse(order)).thenReturn(response);

        mockMvc.perform(get("/api/v1/orders/ext-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalOrderId").value("ext-001"))
                .andExpect(jsonPath("$.status").value("RECEIVED"));
    }

    @Test
    void shouldReturn404WhenOrderNotFound() throws Exception {
        when(getOrderUseCase.execute("not-found")).thenThrow(new OrderNotFoundException("not-found"));

        mockMvc.perform(get("/api/v1/orders/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturn200ForOrderList() throws Exception {
        Order order = buildOrder();
        OrderSummaryResponse summary = new OrderSummaryResponse("ext-001", "customer-1", "RECEIVED", BigDecimal.ZERO);

        when(listOrdersUseCase.execute(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(order), PageRequest.of(0, 20), 1));
        when(mapper.toSummaryResponse(order)).thenReturn(summary);

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].externalOrderId").value("ext-001"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    private Order buildOrder() {
        return Order.create("ext-001", "customer-1",
                List.of(OrderItem.create("product-1", 1, new BigDecimal("10.00"))));
    }

    private CreateOrderRequest buildCreateRequest() {
        return new CreateOrderRequest(
                "ext-001", "customer-1",
                List.of(new OrderItemRequest("product-1", 1, new BigDecimal("10.00")))
        );
    }
}
