package com.habsida.store.controller.merchant;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.response.CustomerResponse;
import com.habsida.store.security.AuthUser;
import com.habsida.store.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchant/customers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
@Tag(name = "Merchant Customers", description = "Merchant: view customers who have ordered from your stores")
public class MerchantCustomerController {

    private final CustomerService customerService;

    @Operation(summary = "List customers who have ordered from the merchant's stores (optionally filter by ?storeId=)")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping
    public PageResponse<CustomerResponse> listCustomersWhoOrdered(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(required = false) Long storeId,
            Pageable pageable) {
        return customerService.findCustomersWhoOrderedFromMerchant(authUser.getId(), storeId, pageable);
    }
}