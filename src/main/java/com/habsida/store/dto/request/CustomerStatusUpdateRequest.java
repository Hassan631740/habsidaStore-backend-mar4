package com.habsida.store.dto.request;

import com.habsida.store.enums.CustomerStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerStatusUpdateRequest {

    @NotNull
    private CustomerStatus status;
}
