package com.habsida.store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {

    private Long id;
    private String streetLine1;
    private String streetLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
}
