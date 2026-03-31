package com.habsida.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Modifier group create/update")
public class ModifierGroupRequest {

    @NotBlank(message = "Name is required")
    @Schema(example = "Size", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotNull(message = "Store ID is required")
    @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long storeId;
}
