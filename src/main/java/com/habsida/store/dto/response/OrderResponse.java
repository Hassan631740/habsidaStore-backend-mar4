package com.habsida.store.dto.response;

import com.habsida.store.enums.OrderStatus;
import com.habsida.store.enums.OrderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private Long customerId;
    private OrderStatus status;
    private OrderType orderType;
    private BigDecimal totalAmount;
    private Instant createdAt;
    private Instant updatedAt;
}
