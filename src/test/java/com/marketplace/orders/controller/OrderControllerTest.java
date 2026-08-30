package com.marketplace.orders.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.orders.dto.*;
import com.marketplace.orders.enums.OrderStatus;
import com.marketplace.orders.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private OrderService orderService;

  private OrderRequest orderRequest;
  private OrderResponse orderResponse;

  @BeforeEach
  void setup() {
    OrderItemRequest itemRequest = new OrderItemRequest(1L, "Product 1", 2, new BigDecimal("50.00"));
    orderRequest = new OrderRequest(1L, List.of(itemRequest));
    OrderItemResponse itemResponse = new OrderItemResponse(1L, 1L, "Product 1", 2, new BigDecimal("50.00"));
    orderResponse = new OrderResponse(1L, 1L, List.of(itemResponse), new BigDecimal("100.0"), OrderStatus.PENDING, LocalDateTime.of(2026, 8, 26, 10, 0));
  }

  @Test
  @DisplayName("Test create order")
  void testCreateOrder() throws Exception {

    when(orderService.create(any(OrderRequest.class))).thenReturn(orderResponse);

    mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(orderRequest)))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(orderResponse.getId()))
        .andExpect(jsonPath("$.customerId").value(orderResponse.getCustomerId()))
        .andExpect(jsonPath("$.total").value(orderResponse.getTotal()))
        .andExpect(jsonPath("$.status").value(orderResponse.getStatus().toString()));

    verify(orderService).create(any(OrderRequest.class));
  }

  @Test
  @DisplayName("Test get id order")
  void testGetOrder() throws Exception {
    Long id = 1L;

    when(orderService.findById(id)).thenReturn(orderResponse);

    mockMvc.perform(get("/api/orders/{id}", id))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(orderResponse.getId()))
        .andExpect(jsonPath("$.customerId").value(orderResponse.getCustomerId()))
        .andExpect(jsonPath("$.status").value(orderResponse.getStatus().toString()));

    verify(orderService).findById(id);
  }

  @Test
  @DisplayName("Test get all orders")
  void testGetAllOrders() throws Exception {

    when(orderService.findAll(1, 3)).thenReturn(new PageResponse<>(List.of(orderResponse), 1, 3, 1, 1));

    mockMvc.perform(get("/api/orders"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content[0].id").value(orderResponse.getId()))
        .andExpect(jsonPath("$.content[0].customerId").value(orderResponse.getCustomerId()))
        .andExpect(jsonPath("$.content[0].total").value(orderResponse.getTotal()));

    verify(orderService).findAll(1, 3);
  }

  @Test
  @DisplayName("Test update order by id")
  void testUpdateOrder() throws Exception {
    Long id = 1L;

    when(orderService.update(eq(id), any(OrderRequest.class))).thenReturn(orderResponse);

    mockMvc.perform((put("/api/orders/{id}", id))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsBytes(orderRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(orderResponse.getId()))
        .andExpect(jsonPath("$.customerId").value(orderResponse.getCustomerId()))
        .andExpect(jsonPath("$.total").value(orderResponse.getTotal()))
        .andExpect(jsonPath("$.status").value(orderResponse.getStatus().toString()));

    verify(orderService).update(eq(id), any(OrderRequest.class));
  }

  @Test
  @DisplayName("Test  dele order by id")
  void testDeleteOrder() throws Exception {
    Long id = 1L;

    mockMvc.perform(delete("/api/orders/{id}", id))
        .andExpect(status().isNoContent());

    verify(orderService).delete(id);
  }

  @Test
  @DisplayName("Test cancel order by id")
  void testCancelOrder() throws Exception {
    Long id  = 1L;

    when(orderService.cancelOrder(eq(id))).thenReturn(orderResponse);

    mockMvc.perform(patch("/api/orders/{id}/cancel", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(orderResponse.getId()))
        .andExpect(jsonPath("$.customerId").value(orderResponse.getCustomerId()))
        .andExpect(jsonPath("$.total").value(orderResponse.getTotal()))
        .andExpect(jsonPath("$.status").value(orderResponse.getStatus().toString()));

    verify(orderService).cancelOrder(eq(id));
  }

  @Test
  @DisplayName("Should confirm order")
  void testConfirmOrder() throws Exception {
    Long id = 1L;

    when(orderService.confirmOrder(eq(id))).thenReturn(orderResponse);

    mockMvc.perform(patch("/api/orders/{id}/confirmed", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(orderRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(orderResponse.getId()))
        .andExpect(jsonPath("$.customerId").value(orderResponse.getCustomerId()))
        .andExpect(jsonPath("$.total").value(orderResponse.getTotal()))
        .andExpect(jsonPath("$.status").value(orderResponse.getStatus().toString()));

    verify(orderService).confirmOrder(eq(id));
  }

  @Test
  @DisplayName("Should ship order")
  void testShipOrder() throws Exception {
    Long id = 1L;
    when(orderService.shipOrder(eq(id))).thenReturn(orderResponse);

    mockMvc.perform(patch("/api/orders/shipped")
            .param("orderId", "1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(orderRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(orderResponse.getId()))
        .andExpect(jsonPath("$.customerId").value(orderResponse.getCustomerId()))
        .andExpect(jsonPath("$.total").value(orderResponse.getTotal()))
        .andExpect(jsonPath("$.status").value(orderResponse.getStatus().toString()));

    verify(orderService).shipOrder(eq(id));
  }


}
