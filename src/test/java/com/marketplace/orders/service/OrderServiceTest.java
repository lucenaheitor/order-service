package com.marketplace.orders.service;

import com.marketplace.orders.dto.*;
import com.marketplace.orders.entity.Order;
import com.marketplace.orders.entity.OrderItem;
import com.marketplace.orders.enums.OrderStatus;
import com.marketplace.orders.exception.BusinessRuleException;
import com.marketplace.orders.exception.ResourceNotFoundException;
import com.marketplace.orders.exception.UnprocessableEntity;
import com.marketplace.orders.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

  @Mock
  private OrderRepository orderRepository;
  @InjectMocks
  private OrderService orderService;

  private OrderResponse orderResponse;
  private OrderRequest orderRequest;
  private Order order;
  private OrderItem orderItem;


  @BeforeEach
  public void setup() {
    LocalDateTime createdAt = LocalDateTime.of(2026, 8, 23, 10, 0);

    order = new Order(1L, 1L, List.of(), new BigDecimal("100.00"), OrderStatus.PENDING, createdAt);
    orderItem = new OrderItem(1L, order, 1L, "Product 1", 2, new BigDecimal("50.00"));
    order.setItems(List.of(orderItem));
    orderResponse = new OrderResponse(1L, 1L, List.of(new OrderItemResponse(1L, 1L, "Product 1", 2, new BigDecimal("50.00"))), new BigDecimal("100.00"), OrderStatus.PENDING, createdAt);
    orderRequest = new OrderRequest(1L, List.of(new OrderItemRequest(1L, "Product 1", 2, new BigDecimal("50.00"))));
  }

  @Test
  @DisplayName("Test OrderService create method")
  public void testCreateOrder() {

    when(orderRepository.save(any(Order.class))).thenReturn(order);

    OrderResponse result = orderService.create(orderRequest);

    assertNotNull(result);
    assertEquals(1L, result.getCustomerId());
    assertEquals(new BigDecimal("100.00"), result.getTotal());
    assertEquals(OrderStatus.PENDING, result.getStatus());
    assertEquals(1, result.getItems().size());
    assertNotNull(result.getCreatedAt());

    verify(orderRepository).save(any(Order.class));
  }

  @Test
  @DisplayName("Test OrderService create method with null customerId")
  public void shouldThrowExceptionWhenCustomerIdIsNull() {
    orderRequest.setCustomerId(null);

    BusinessRuleException exception = assertThrows(
        BusinessRuleException.class,
        () -> orderService.create(orderRequest)
    );

    assertEquals("Customer ID is required", exception.getMessage());
    verify(orderRepository, never()).save(any(Order.class));

  }


  @Test
  @DisplayName("Test OrderService create method with  null items")
    void shouldReturnItmsNull(){
      orderRequest.setItems(null);

      BusinessRuleException expection = assertThrows(
          BusinessRuleException.class,
          () -> orderService.create(orderRequest)
      );

      assertEquals("Items cannot be empty", expection.getMessage());
      verify(orderRepository, never()).save(any(Order.class));
    }

  @Test
  @DisplayName("should return  orderResponse when findById is called with valid id")
  void shouldReturnOrderResponseWhenFindByIdIsCalledWithValidId() {

    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    OrderResponse result = orderService.findById(1L);

    assertNotNull(result);
    assertEquals(1L, result.getCustomerId());

    verify(orderRepository).findById(1L);
  }

  @Test
  @DisplayName("should throw ResourceNotFoundException when  not found ID")
  void shouldThrowResourceNotFoundExceptionWhenFindByIdIsCalledWithInvalidId() {

    when(orderRepository.findById(1L)).thenReturn(Optional.empty());

    ResourceNotFoundException exception = assertThrows(
        ResourceNotFoundException.class,
        () -> orderService.findById(1L));

    assertEquals("Order not found with id: 1", exception.getMessage());
    verify(orderRepository, times(1)).findById(1L);
  }

  @Test
  @DisplayName(" should cancel order")
  void shouldCancelOrder() {
    Long Id = 1L;
    when(orderRepository.findById(Id)).thenReturn(Optional.of(order));
    when(orderRepository.save(order)).thenReturn(order);

    OrderResponse result = orderService.cancelOrder(Id);

    assertNotNull(result);
    assertEquals(OrderStatus.CANCELLED, result.getStatus());
    verify(orderRepository, times(1)).findById(Id);
    verify(orderRepository, times(1)).save(order);
  }

  @Test
  @DisplayName("should throw BusinessRuleException when cancel order with status CANCELLED")
  void shouldThrowBusinessRuleExceptionWhenCancelOrderWithStatusCANCELLED() {
    Long id = 1L;

    Order order = new Order(1L, 1L, List.of(), new BigDecimal("100.00"), OrderStatus.CANCELLED, LocalDateTime.of(2026, 8, 23, 10, 0));

    when(orderRepository.findById(id)).thenReturn(Optional.of(order));

    BusinessRuleException exception =  assertThrows(
        BusinessRuleException.class,
        () -> orderService.cancelOrder(id)
    );

    assertEquals("Cannot cancelled order", exception.getMessage());

    verify(orderRepository, times(1)).findById(id);
    verify(orderRepository, never()).save(any(Order.class));
  }


  @Test
  @DisplayName("should  return OrderResponse when confirm order")
  void shouldReturnOrderResponseWhenConfirmOrder() {
    Long id = 1L;

    when(orderRepository.findById(id)).thenReturn(Optional.of(order));
    when(orderRepository.save(order)).thenReturn(order);

    OrderResponse result = orderService.confirmOrder(id);

    assertNotNull(result);
    assertEquals(1L, result.getCustomerId());
    assertEquals(OrderStatus.CONFIRMED, result.getStatus());

    verify(orderRepository, times(1)).findById(id);
    verify(orderRepository, times(1)).save(order);
  }

  @Test
  @DisplayName("should  UnprocessableEntity when confirm order with status not PENDING")
  void shouldThrowUnprocessableEntityWhenConfirmOrderWithStatusPENDING() {
    Long id = 1L;
    order = new Order(1L, 1L, List.of(), new BigDecimal("100.00"), OrderStatus.CANCELLED, LocalDateTime.of(2026, 8, 23, 10, 0));


    when(orderRepository.findById(id)).thenReturn(Optional.of(order));

    UnprocessableEntity exception = assertThrows(
        UnprocessableEntity.class,
        () -> orderService.confirmOrder(id)
    );

    assertEquals("Only Pending orders can be confirmed", exception.getMessage());
    verify(orderRepository, times(1)).findById(id);
    verify(orderRepository, never()).save(any(Order.class));

  }


  @Test
  @DisplayName("should return SHIPPED when ship order")
  void shouldReturnShippedOrder() {
    Long id = 1L;
    order = new Order(1L, 1L, List.of(), new BigDecimal("100.00"), OrderStatus.CONFIRMED,  LocalDateTime.of(2026, 8, 23, 10, 0));


    when(orderRepository.findById(id)).thenReturn(Optional.of(order));
    when(orderRepository.save(order)).thenReturn(order);

    OrderResponse result = orderService.shipOrder(id);

    assertNotNull(result);
    assertEquals(id, result.getCustomerId());
    assertEquals(OrderStatus.SHIPPED, result.getStatus());

    verify(orderRepository).findById(id);
    verify(orderRepository).save(order);
  }

  @Test
  @DisplayName("should throw BusinessRuleException when ship order with status not CONFIRMED")
  void shouldThrowBusinessRuleExceptionWhenShipOrderWithStatusNotConfirmed() {
    Long id = 1L;

    when(orderRepository.findById(id)).thenReturn(Optional.of(order));

    BusinessRuleException exception =  assertThrows(
        BusinessRuleException.class,
        () -> orderService.shipOrder(id)
    );

    assertEquals("Only Confirmed orders can be shipped", exception.getMessage());

    verify(orderRepository, times(1)).findById(id);
    verify(orderRepository, never()).save(any(Order.class));
  }

  @Test
  @DisplayName("should return  update new  Order")
    void shouldReturnUpdateOrder() {
    Long id = 1L;

    when(orderRepository.findById(id)).thenReturn(Optional.of(order));
    when(orderRepository.save(any(Order.class))).thenReturn(order);

    OrderResponse response = orderService.update(id, orderRequest);
    assertNotNull(response);
    assertEquals(1L, response.getId());
    assertEquals(1L, response.getCustomerId());
    assertEquals(1, response.getItems().size());
    assertEquals(new BigDecimal("100.00"), response.getTotal());
    assertEquals(OrderStatus.PENDING, response.getStatus());

    verify(orderRepository).findById(id);
    verify(orderRepository).save(order);
    }

  @Test
  @DisplayName("should return all orders with pagination")
  void shouldReturnAllOrderWithPageable() {
    int page = 1;
    int pageSize = 10;
    PageRequest pageRequest = PageRequest.of(
        page - 1,
        pageSize,
        Sort.by(Sort.Direction.DESC, "createdAt")
    );
    Page<Order> orderPage = new PageImpl<>(List.of(order), pageRequest, 1);

    when(orderRepository.findAll(pageRequest)).thenReturn(orderPage);

    PageResponse<OrderResponse> response = orderService.findAll(page, pageSize);

    assertNotNull(response);
    assertEquals(1, response.page());
    assertEquals(10, response.size());
    assertEquals(1, response.totalElements());
    assertEquals(1, response.totalPages());
    assertEquals(1, response.content().size());
    assertEquals(order.getId(), response.content().getFirst().getId());
    assertEquals(order.getCustomerId(), response.content().getFirst().getCustomerId());

    verify(orderRepository).findAll(pageRequest);
  }

  @Test
  @DisplayName("should delete order by id")
  void shouldDeleteOrderById() {
    Long id = 1L;

    orderService.delete(id);

    verify(orderRepository).deleteById(id);
  }

}
