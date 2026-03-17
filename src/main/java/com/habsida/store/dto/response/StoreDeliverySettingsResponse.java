package com.habsida.store.dto.response;

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
public class StoreDeliverySettingsResponse {

    private Long id;
    private Long storeId;
    private BigDecimal deliveryFee;
    private BigDecimal minOrderAmount;
    private Instant createdAt;
    private Instant updatedAt;
}
