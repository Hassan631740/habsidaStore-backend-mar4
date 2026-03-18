package com.habsida.store.dto.response;

import com.habsida.store.enums.CustomerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCustomerDetailResponse {

    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String phone;
    private CustomerStatus status;
    private List<AddressResponse> addresses;
    private Instant createdAt;
    private Instant updatedAt;
}
