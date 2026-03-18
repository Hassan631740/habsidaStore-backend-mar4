package com.habsida.store.dto.request;

import com.habsida.store.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantOrderStatusRequest {

    @NotNull
    private OrderStatus status;
}
