package com.habsida.store.dto.request;

import com.habsida.store.enums.StoreStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@io.swagger.v3.oas.annotations.media.Schema(description = "Store create/update", example = "{\"name\":\"Main Store\",\"addressId\":1,\"status\":\"ACTIVE\"}")
public class StoreRequest {

    @NotBlank(message = "Name is required")
    @io.swagger.v3.oas.annotations.media.Schema(example = "Main Store", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
    private String name;

    @NotNull(message = "Address ID is required")
    @Positive(message = "Address ID must be positive")
    @io.swagger.v3.oas.annotations.media.Schema(example = "1", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
    private Long addressId;

    @io.swagger.v3.oas.annotations.media.Schema(example = "ACTIVE")
    private StoreStatus status;
}
