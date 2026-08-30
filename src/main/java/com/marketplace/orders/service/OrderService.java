package com.marketplace.orders.service;

import com.marketplace.orders.dto.OrderRequest;
import com.marketplace.orders.dto.OrderResponse;
import com.marketplace.orders.dto.PageResponse;
import com.marketplace.orders.entity.Order;
import com.marketplace.orders.entity.OrderItem;
import com.marketplace.orders.enums.OrderStatus;
import com.marketplace.orders.exception.BusinessRuleException;
import com.marketplace.orders.exception.ResourceNotFoundException;
import com.marketplace.orders.exception.UnprocessableEntity;
import com.marketplace.orders.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

  private static final int MAX_PAGE_SIZE = 100;

  private final OrderRepository orderRepository;

  public OrderResponse create(OrderRequest request) {

    if (request.getCustomerId() == null) {
      throw new BusinessRuleException("Customer ID is required");
    }
    if (request.getItems() == null || request.getItems().isEmpty()) {
      throw new BusinessRuleException("Items cannot be empty");
    }

      Order order = Order.builder()
          .customerId(request.getCustomerId())
          .total(request.getItems().stream()
              .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
              .reduce(BigDecimal.ZERO, BigDecimal::add))
          .status(OrderStatus.PENDING)
          .createdAt(LocalDateTime.now())
          .build();

      List<OrderItem> items = request.getItems().stream()
          .map(itemRequest -> OrderItem.builder()
              .order(order)                              // ← objeto Order
              .productId(itemRequest.getProductId())
              .productName(itemRequest.getProductName())
              .quantity(itemRequest.getQuantity())
              .unitPrice(itemRequest.getUnitPrice())
              .build())
          .collect(Collectors.toList());

      order.setItems(items);
      orderRepository.save(order); // um save só agora!

      return OrderResponse.fromEntity(order);
  }

  public OrderResponse findById(Long id) {
    Order order = orderRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id ));

    return OrderResponse.fromEntity(order);
  }

  public PageResponse<OrderResponse> findAll(int page, int size) {
    validatePagination(page, size);

    PageRequest pageRequest = PageRequest.of(
        page - 1,
        size,
        Sort.by(Sort.Direction.DESC, "createdAt")
    );

    return PageResponse.from(
        orderRepository.findAll(pageRequest)
            .map(OrderResponse::fromEntity)
    );
  }

  private void validatePagination(int page, int size) {
    if (page < 1) {
      throw new UnprocessableEntity("Page must be greater than zero");
    }

    if (size < 1 || size > MAX_PAGE_SIZE) {
      throw new UnprocessableEntity(
          "Size must be between 1 and " + MAX_PAGE_SIZE
      );
    }
  }

  public OrderResponse update(Long id, OrderRequest request) {
    Order order = orderRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

    if (request.getCustomerId() != null) {
      order.setCustomerId(request.getCustomerId());
    }
    if (order.getStatus().equals(OrderStatus.CANCELLED)) {
      throw new BusinessRuleException("Cannot update cancelled order");
    }
    if (request.getItems() != null && !request.getItems().isEmpty()) {
      List<OrderItem> items = request.getItems().stream()
          .map(itemRequest -> OrderItem.builder()
              .order(order)
              .productId(itemRequest.getProductId())
              .productName(itemRequest.getProductName())
              .quantity(itemRequest.getQuantity())
              .unitPrice(itemRequest.getUnitPrice())
              .build())
          .collect(Collectors.toList());
      order.setItems(items);
      order.setTotal(items.stream()
          .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
          .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    Order updatedOrder = orderRepository.save(order);
    return OrderResponse.fromEntity(updatedOrder);
  }

  public void delete(Long id) {
    orderRepository.deleteById(id);
  }

  public  OrderResponse cancelOrder(Long id) {
    Order  order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

    validateCanceled(order);

    order.setStatus(OrderStatus.CANCELLED);
    orderRepository.save(order);

    return OrderResponse.fromEntity(order);
  }

  private void validateCanceled(Order order) {
    if (order.getStatus().equals(OrderStatus.CANCELLED)) {
      throw new BusinessRuleException("Cannot cancelled order");
    }

    if (order.getStatus().equals(OrderStatus.DELIVERED)) {
      throw new BusinessRuleException("Cannot cancel order");
    }

  }

  public  OrderResponse confirmOrder(Long id) {
    Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

    if(!OrderStatus.PENDING.equals(order.getStatus())) {
      throw new UnprocessableEntity("Only Pending orders can be confirmed");
    }

    order.setStatus(OrderStatus.CONFIRMED);
    orderRepository.save(order);

    return OrderResponse.fromEntity(order);
  }


  public  OrderResponse shipOrder(Long id) {
    Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

     if(!OrderStatus.CONFIRMED.equals(order.getStatus())) {
       throw new BusinessRuleException("Only Confirmed orders can be shipped");
     }

    order.setStatus(OrderStatus.SHIPPED);
    orderRepository.save(order);

    return OrderResponse.fromEntity(order);
  }

}
