package com.habsida.store.dto.request;

import com.habsida.store.enums.CustomerStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCustomerCreateRequest {

    private Long userId;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String phone;

    @Builder.Default
    private CustomerStatus status = CustomerStatus.ACTIVE;

    @Valid
    @Builder.Default
    private List<AddressRequest> addresses = new ArrayList<>();
}
