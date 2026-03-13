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
public class CustomerResponse {

    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String phone;
    private Instant createdAt;
    private Instant updatedAt;
}
