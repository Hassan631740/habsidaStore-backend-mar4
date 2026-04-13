package com.habsida.store.controller.merchant;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.response.CustomerResponse;
import com.habsida.store.security.AuthUser;
import com.habsida.store.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Customers who have placed at least one order containing products from the merchant's store(s).
 */
@RestController
@RequestMapping("/api/merchant/customers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
public class MerchantCustomerController {

    private final CustomerService customerService;

    @GetMapping
    public PageResponse<CustomerResponse> listCustomersWhoOrdered(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(required = false) Long storeId,
            Pageable pageable) {
        return customerService.findCustomersWhoOrderedFromMerchant(authUser.getId(), storeId, pageable);
    }
}