package com.marketplace.orders.dto;

import com.marketplace.orders.entity.Order;
import com.marketplace.orders.enums.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Long id;
    private Long customerId;
    private List<OrderItemResponse> items;
    private BigDecimal total;
    private OrderStatus status;
    private LocalDateTime createdAt;

    public static OrderResponse fromEntity(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getItems().stream()
                        .map(OrderItemResponse::fromEntity)
                        .collect(Collectors.toList()),
                order.getTotal(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }

}
