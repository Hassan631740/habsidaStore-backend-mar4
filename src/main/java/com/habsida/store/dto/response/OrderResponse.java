package com.habsida.store.dto.response;

import com.habsida.store.enums.OrderStatus;
import com.habsida.store.enums.OrderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private Long storeId;
    private Long customerId;
    private OrderStatus status;
    private OrderType orderType;
    private BigDecimal totalAmount;
    private Instant acceptedAt;
    private String rejectReason;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;
    private List<OrderItemResponse> items;
}
