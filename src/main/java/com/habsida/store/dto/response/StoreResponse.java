package com.habsida.store.dto.response;

import com.habsida.store.enums.StoreStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreResponse {

    private Long id;
    private String name;
    private Long addressId;
    private StoreStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
