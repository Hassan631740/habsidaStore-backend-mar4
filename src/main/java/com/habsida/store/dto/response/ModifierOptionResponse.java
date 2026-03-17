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
public class ModifierOptionResponse {

    private Long id;
    private Long modifierGroupId;
    private String name;
    private BigDecimal priceAdjustment;
    private Instant createdAt;
    private Instant updatedAt;
}
