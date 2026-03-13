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
public class OrderAddressRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Address ID is required")
    private Long addressId;
}
