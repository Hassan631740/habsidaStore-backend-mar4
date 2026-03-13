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
public class StoreHoursRequest {

    @NotNull(message = "Store ID is required")
    private Long storeId;

    @NotNull(message = "Day of week is required")
    private Integer dayOfWeek;

    private LocalTime openTime;
    private LocalTime closeTime;
}
