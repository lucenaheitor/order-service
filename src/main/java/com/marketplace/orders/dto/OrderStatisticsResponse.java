package com.marketplace.orders.dto;

import com.marketplace.orders.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatisticsResponse {
  private long totalOrders;
  private BigDecimal totalRevenue;
  private Map<OrderStatus, Long> countByStatus;
  private BigDecimal averageTicket;
}
