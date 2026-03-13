package com.habsida.store.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {

    private String streetLine1;
    private String streetLine2;

    @NotBlank(message = "City is required")
    private String city;

    private String state;
    private String postalCode;

    @NotBlank(message = "Country is required")
    private String country;
}
