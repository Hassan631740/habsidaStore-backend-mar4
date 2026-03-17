package com.habsida.store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreBreaksResponse {

    private Long id;
    private Long storeId;
    private LocalTime startTime;
    private LocalTime endTime;
}
