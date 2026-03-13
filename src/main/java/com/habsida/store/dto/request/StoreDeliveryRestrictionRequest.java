package com.habsida.store.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreDeliveryRestrictionRequest {

    @NotNull(message = "Store ID is required")
    private Long storeId;

    @NotBlank(message = "Type is required")
    private String type;

    private String value;
}
