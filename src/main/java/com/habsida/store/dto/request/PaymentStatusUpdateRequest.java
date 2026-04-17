package com.habsida.store.dto.request;

import com.habsida.store.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private PaymentStatus status;
}