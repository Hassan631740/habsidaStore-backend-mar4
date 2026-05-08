package com.habsida.store.controller.admin;

import com.habsida.store.dto.request.AssignMerchantRequest;
import com.habsida.store.dto.response.UserStoreAccessResponse;
import com.habsida.store.service.UserStoreAccessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/**
 * Admin: assign or remove merchant (user) to/from a store. RBAC: gives the user access to manage that store's catalog.
 */
@RestController
@RequestMapping("/api/admin/stores/{storeId}/merchants")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Stores", description = "Store management (admin: any store; merchant: assigned stores)")
public class AdminStoreMerchantController {

    private final UserStoreAccessService userStoreAccessService;

    @GetMapping
    public List<UserStoreAccessResponse> listAssigned(@PathVariable Long storeId) {
        return userStoreAccessService.listByStore(storeId);
    }

    @PostMapping
    public ResponseEntity<UserStoreAccessResponse> assignMerchant(
            @PathVariable Long storeId,
            @Valid @RequestBody AssignMerchantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userStoreAccessService.assignMerchant(storeId, request));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> unassignMerchant(@PathVariable Long storeId, @PathVariable Long userId) {
        userStoreAccessService.unassignMerchant(storeId, userId);
        return ResponseEntity.noContent().build();
    }
}