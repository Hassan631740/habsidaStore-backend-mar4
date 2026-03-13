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
public class ProductImageRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "URL is required")
    private String url;

    private Integer sortOrder;
}
