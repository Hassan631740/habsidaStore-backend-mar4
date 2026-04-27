package com.habsida.store.controller.merchant;

import com.habsida.store.dto.request.*;
import com.habsida.store.dto.response.*;
import com.habsida.store.exception.ResourceNotFoundException;
import com.habsida.store.security.AuthUser;
import com.habsida.store.service.MerchantStoreAccessService;
import com.habsida.store.service.StoreSettingsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Merchant endpoints for store settings. Merchant can only manage stores they have access to.
 * Requires ROLE_MERCHANT.
 */
@RestController
@RequestMapping("/api/merchant/settings/{storeId}")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
@Tag(name = "Store Settings (Merchant)", description = "Merchant: manage delivery, hours, and breaks for own stores")
public class MerchantStoreSettingsController {

    private final StoreSettingsService settingsService;
    private final MerchantStoreAccessService merchantStoreAccessService;

    // ---- Delivery Settings ----

    @GetMapping("/delivery")
    public ResponseEntity<StoreDeliverySettingsResponse> getDeliverySettings(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long storeId) {
        assertAccess(authUser.getId(), storeId);
        return ResponseEntity.ok(settingsService.getDeliverySettings(storeId));
    }

    @PutMapping("/delivery")
    public ResponseEntity<StoreDeliverySettingsResponse> upsertDeliverySettings(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long storeId,
            @Valid @RequestBody StoreDeliverySettingsRequest request) {
        assertAccess(authUser.getId(), storeId);
        return ResponseEntity.ok(settingsService.upsertDeliverySettings(storeId, request));
    }

    // ---- Delivery Areas ----

    @GetMapping("/areas")
    public ResponseEntity<List<StoreDeliveryAreaResponse>> getDeliveryAreas(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long storeId) {
        assertAccess(authUser.getId(), storeId);
        return ResponseEntity.ok(settingsService.getDeliveryAreas(storeId));
    }

    @PutMapping("/areas")
    public ResponseEntity<List<StoreDeliveryAreaResponse>> replaceDeliveryAreas(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long storeId,
            @RequestBody List<@Valid StoreDeliveryAreaRequest> requests) {
        assertAccess(authUser.getId(), storeId);
        return ResponseEntity.ok(settingsService.replaceDeliveryAreas(storeId, requests));
    }

    // ---- Delivery Restrictions ----

    @GetMapping("/restrictions")
    public ResponseEntity<List<StoreDeliveryRestrictionResponse>> getDeliveryRestrictions(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long storeId) {
        assertAccess(authUser.getId(), storeId);
        return ResponseEntity.ok(settingsService.getDeliveryRestrictions(storeId));
    }

    @PutMapping("/restrictions")
    public ResponseEntity<List<StoreDeliveryRestrictionResponse>> replaceDeliveryRestrictions(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long storeId,
            @RequestBody List<@Valid StoreDeliveryRestrictionRequest> requests) {
        assertAccess(authUser.getId(), storeId);
        return ResponseEntity.ok(settingsService.replaceDeliveryRestrictions(storeId, requests));
    }

    // ---- Work Hours ----

    @GetMapping("/hours")
    public ResponseEntity<List<StoreHoursResponse>> getWorkHours(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long storeId) {
        assertAccess(authUser.getId(), storeId);
        return ResponseEntity.ok(settingsService.getWorkHours(storeId));
    }

    @PutMapping("/hours")
    public ResponseEntity<List<StoreHoursResponse>> replaceWorkHours(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long storeId,
            @RequestBody List<@Valid StoreHoursRequest> requests) {
        assertAccess(authUser.getId(), storeId);
        return ResponseEntity.ok(settingsService.replaceWorkHours(storeId, requests));
    }

    // ---- Breaks ----

    @GetMapping("/breaks")
    public ResponseEntity<List<StoreBreaksResponse>> getBreaks(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long storeId) {
        assertAccess(authUser.getId(), storeId);
        return ResponseEntity.ok(settingsService.getBreaks(storeId));
    }

    @PutMapping("/breaks")
    public ResponseEntity<List<StoreBreaksResponse>> replaceBreaks(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long storeId,
            @RequestBody List<@Valid StoreBreaksRequest> requests) {
        assertAccess(authUser.getId(), storeId);
        return ResponseEntity.ok(settingsService.replaceBreaks(storeId, requests));
    }

    // ---- helpers ----

    private void assertAccess(Long userId, Long storeId) {
        List<Long> storeIds = merchantStoreAccessService.getStoreIds(userId);
        if (!storeIds.contains(storeId)) {
            throw new ResourceNotFoundException("Store", storeId);
        }
    }
}