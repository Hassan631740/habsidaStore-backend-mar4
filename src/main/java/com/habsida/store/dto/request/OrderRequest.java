package com.habsida.store.dto.request;

import com.habsida.store.enums.OrderStatus;
import com.habsida.store.enums.OrderType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    @NotNull(message = "Customer ID is required")
    @Positive(message = "Customer ID must be positive")
    private Long customerId;

    @NotNull(message = "Status is required")
    private OrderStatus status;

    private OrderType orderType;

    @PositiveOrZero(message = "Total amount must be zero or positive")
    private BigDecimal totalAmount;
}
