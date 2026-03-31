package com.habsida.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Category create/update")
public class CategoryRequest {

    @NotBlank(message = "Name is required")
    @Schema(example = "Beverages", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(example = "beverages")
    private String slug;
    private Long parentId;

    /** Required for create; for Admin store-scoped create, path storeId overrides when matching. */
    @Positive(message = "Store ID must be positive")
    @Schema(example = "1")
    private Long storeId;
}
