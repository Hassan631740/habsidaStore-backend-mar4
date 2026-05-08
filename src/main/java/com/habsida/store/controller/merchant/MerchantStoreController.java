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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchant/stores")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
@Tag(name = "Stores", description = "Store management (admin: any store; merchant: assigned stores)")
public class MerchantStoreController {

    private final StoreService storeService;

    @Operation(summary = "List stores assigned to the authenticated merchant")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping
    public PageResponse<StoreResponse> findMyStores(
            @AuthenticationPrincipal AuthUser authUser,
            Pageable pageable) {
        return storeService.findAllForMerchant(authUser.getId(), pageable);
    }

    @Operation(summary = "Get a store by ID (must belong to the authenticated merchant)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "403", description = "Store not assigned to this merchant"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<StoreResponse> findById(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id) {
        return ResponseEntity.ok(storeService.getByIdForMerchant(authUser.getId(), id));
    }
}
