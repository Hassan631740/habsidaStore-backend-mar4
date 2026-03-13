package com.habsida.store.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductModifierGroupRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Modifier group ID is required")
    private Long modifierGroupId;
}
