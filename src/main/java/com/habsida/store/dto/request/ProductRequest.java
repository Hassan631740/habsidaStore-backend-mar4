package com.habsida.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Product create/update")
public class ProductRequest {

    @NotBlank(message = "Name is required")
    @Schema(example = "Espresso", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(example = "Single shot espresso")
    private String description;

    @NotNull(message = "Price is required")
    @PositiveOrZero(message = "Price must be zero or positive")
    @Schema(example = "2.50", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal price;

    @NotNull(message = "Category ID is required")
    @Positive(message = "Category ID must be positive")
    @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long categoryId;

    @NotNull(message = "Store ID is required")
    @Positive(message = "Store ID must be positive")
    @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long storeId;

    /** When false, ordering is paused for this product. Default true. */
    @Schema(example = "true")
    private Boolean availableForOrder;
}
