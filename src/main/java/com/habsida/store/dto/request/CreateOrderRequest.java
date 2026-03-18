package com.habsida.store.dto.request;

import com.habsida.store.enums.OrderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request body for POST /api/admin/stores/{storeId}/orders (admin/system create order).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotNull
    @Positive
    private Long customerId;

    private String notes;
    private OrderType orderType;

    @NotEmpty
    @Valid
    private List<CreateOrderLineRequest> lines;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateOrderLineRequest {
        @NotNull
        @Positive
        private Long productId;
        @NotNull
        @Positive
        private Integer quantity;
        /** Modifier option IDs to attach to this line (optional). */
        private List<@Positive Long> modifierOptionIds;
    }
}
