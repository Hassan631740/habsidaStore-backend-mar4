package com.habsida.store.dto.request;

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
public class CategoryRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String slug;
    private Long parentId;

    /** Required for create; for Admin store-scoped create, path storeId overrides when matching. */
    @Positive(message = "Store ID must be positive")
    private Long storeId;
}
