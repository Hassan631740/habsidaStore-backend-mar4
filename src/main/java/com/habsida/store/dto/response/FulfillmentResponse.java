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
public class FulfillmentResponse {

    private Long id;
    private Long orderId;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
