package com.habsida.store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.habsida.store.enums.CustomerStatus;

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
    private CustomerStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
