package com.habsida.store.controller.merchant;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.response.StoreResponse;
import com.habsida.store.security.AuthUser;
import com.habsida.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Merchant-scoped store access. Lists only stores the current user has access to. Requires ROLE_MERCHANT.
 */
@RestController
@RequestMapping("/api/merchant/stores")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
@Tag(name = "Stores", description = "Store management (admin: any store; merchant: assigned stores)")
public class MerchantStoreController {

    private final StoreService storeService;

    @GetMapping
    public PageResponse<StoreResponse> findMyStores(
            @AuthenticationPrincipal AuthUser authUser,
            Pageable pageable) {
        return storeService.findAllForMerchant(authUser.getId(), pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreResponse> findById(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id) {
        return ResponseEntity.ok(storeService.getByIdForMerchant(authUser.getId(), id));
    }
}
