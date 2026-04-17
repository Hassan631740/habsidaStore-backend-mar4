package com.habsida.store.dto.request;

import com.habsida.store.enums.StoreStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private StoreStatus status;
}