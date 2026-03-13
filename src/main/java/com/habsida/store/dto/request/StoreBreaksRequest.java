package com.habsida.store.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreBreaksRequest {

    @NotNull(message = "Store ID is required")
    private Long storeId;

    private LocalTime startTime;
    private LocalTime endTime;
}
