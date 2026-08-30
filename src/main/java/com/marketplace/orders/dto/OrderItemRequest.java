package com.marketplace.orders.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemRequest {

    @NotNull
    private Long productId;

    @NotBlank
    private String productName;

    @Positive
    private int quantity;

    @NotNull
    @Positive
    private BigDecimal unitPrice;

}
