package com.habsida.store.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantOrderRejectRequest {

    /** Optional reason for rejection (stored in order.reject_reason). */
    private String rejectReason;
}
