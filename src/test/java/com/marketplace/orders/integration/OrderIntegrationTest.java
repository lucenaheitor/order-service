package com.marketplace.orders.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.orders.dto.OrderItemRequest;
import com.marketplace.orders.dto.OrderRequest;
import com.marketplace.orders.entity.Order;
import com.marketplace.orders.enums.OrderStatus;
import com.marketplace.orders.repository.OrderRepository;
import com.marketplace.orders.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
@Transactional
class OrderIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private OrderRepository orderRepository;

  private OrderItemRequest orderItemRequest;
  private OrderRequest orderRequest;
  private Order order;

  @BeforeEach
  void setUp() {
    orderItemRequest = new OrderItemRequest(1L, "Product 1", 2, new BigDecimal("50.00"));
    orderRequest = new OrderRequest(1L, List.of(orderItemRequest));
    order = new Order(null, 1L, List.of(), new BigDecimal("100.00"), OrderStatus.PENDING, LocalDateTime.of(2026, 8, 30, 10, 0));
  }

  @Test
  @DisplayName("Test creating an order")
  void testCreateOrder() throws Exception {

    mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(orderRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.total").value(100.00));

    Order result = orderRepository.findAll().getFirst();

    assertEquals(1L, result.getCustomerId());
    assertEquals(OrderStatus.PENDING, result.getStatus());
    assertEquals("100.00", result.getTotal().toString());
  }
}
