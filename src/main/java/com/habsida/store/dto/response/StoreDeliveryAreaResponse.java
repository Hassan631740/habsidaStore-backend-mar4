package com.habsida.store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreDeliveryAreaResponse {

    private Long id;
    private Long storeId;
    private String name;
    private java.math.BigDecimal fee;
}
