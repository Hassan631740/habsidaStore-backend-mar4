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
public class StoreRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Address ID is required")
    @Positive(message = "Address ID must be positive")
    private Long addressId;

    private StoreStatus status;
}
