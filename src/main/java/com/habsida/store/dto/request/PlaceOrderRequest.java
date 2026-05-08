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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderRequest {

    /**
     * Required when placing as {@code ROLE_ADMIN} (on behalf of a customer).
     * Omitted or ignored for non-admin callers — the server sets it from the authenticated user's customer profile.
     */
    @Positive
    private Long customerId;

    @NotNull
    @Positive
    private Long storeId;

    private OrderType orderType;

    @NotEmpty
    @Valid
    private List<OrderLineRequest> lines;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderLineRequest {
        @NotNull
        @Positive
        private Long productId;
        @NotNull
        @Positive
        private Integer quantity;
        private List<@Positive Long> modifierOptionIds;
    }
}
