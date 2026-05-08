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
@Schema(description = "Admin/system create-order request for a specific store")
public class CreateOrderRequest {

    @NotNull
    @Positive
    @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long customerId;

    @Schema(example = "Extra napkins please")
    private String notes;

    @Schema(example = "DELIVERY")
    private OrderType orderType;

    @NotEmpty
    @Valid
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CreateOrderLineRequest> lines;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "One line item in the order")
    public static class CreateOrderLineRequest {

        @NotNull
        @Positive
        @Schema(example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long productId;

        @NotNull
        @Positive
        @Schema(example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer quantity;

        @Schema(description = "Optional modifier option IDs (e.g. size, extras)", example = "[20, 21]")
        private List<@Positive Long> modifierOptionIds;
    }
}
