package com.habsida.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Modifier option create/update")
public class ModifierOptionRequest {

    @NotNull(message = "Modifier group ID is required")
    @Schema(example = "1")
    private Long modifierGroupId;

    @NotBlank(message = "Name is required")
    @Schema(example = "Large", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(example = "1.00")
    private BigDecimal priceAdjustment;
}
