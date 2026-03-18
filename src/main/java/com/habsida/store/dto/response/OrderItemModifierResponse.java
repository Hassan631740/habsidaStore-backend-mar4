package com.habsida.store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemModifierResponse {

    private Long id;
    private Long orderItemId;
    private Long modifierOptionId;
    private String optionNameSnapshot;
    private BigDecimal price;
}
