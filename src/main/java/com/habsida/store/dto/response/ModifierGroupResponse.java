package com.habsida.store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModifierGroupResponse {

    private Long id;
    private String name;
    private Long storeId;
    private Instant createdAt;
    private Instant updatedAt;
}
