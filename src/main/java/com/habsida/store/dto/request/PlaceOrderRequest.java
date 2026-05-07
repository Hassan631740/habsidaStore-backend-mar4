package com.habsida.store.dto.request;

import com.habsida.store.enums.OrderType;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Customer self-service place-order request")
public class PlaceOrderRequest {

    @Positive
    @Schema(description = "Admin only: override customer. Omit when calling as ROLE_CUSTOMER.", example = "1")
    private Long customerId;

    @NotNull
    @Positive
    @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long storeId;

    @Schema(example = "PICKUP")
    private OrderType orderType;

    @NotEmpty
    @Valid
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private List<OrderLineRequest> lines;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "One line item in the order")
    public static class OrderLineRequest {

        @NotNull
        @Positive
        @Schema(example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long productId;

        @NotNull
        @Positive
        @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer quantity;

        @Schema(description = "Optional modifier option IDs", example = "[20]")
        private List<@Positive Long> modifierOptionIds;
    }
}
