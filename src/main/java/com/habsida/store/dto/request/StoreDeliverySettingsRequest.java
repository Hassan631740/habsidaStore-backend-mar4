package com.habsida.store.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreDeliverySettingsRequest {

    @NotNull(message = "Store ID is required")
    private Long storeId;

    private BigDecimal deliveryFee;
    private BigDecimal minOrderAmount;
}
