package com.habsida.store.controller.merchant;

import com.habsida.store.dto.request.*;
import com.habsida.store.dto.response.*;
import com.habsida.store.exception.ResourceNotFoundException;
import com.habsida.store.security.AuthUser;
import com.habsida.store.service.MerchantStoreAccessService;
import com.habsida.store.service.StoreSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/merchant/settings/{storeId}")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
@Tag(name = "Store Settings (Merchant)", description = "Merchant: manage delivery, hours, and breaks for own stores")
public class MerchantStoreSettingsController {

    private final StoreSettingsService settingsService;
    private final MerchantStoreAccessService merchantStoreAccessService;

    @Operation(summary = "Get delivery settings for the merchant's store")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping("/delivery")
    public ResponseEntity<StoreDeliverySettingsResponse> getDeliverySettings(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long storeId) {
        assertAccess(authUser.getId(), storeId);
        return ResponseEntity.ok(settingsService.getDeliverySettings(storeId));
    }

    @Operation(summary = "Create or update delivery settings for the merchant's store")
    @ApiResponse(responseCode = "200", description = "OK")
    @PutMapping("/delivery")
    public ResponseEntity<StoreDeliverySettingsResponse> upsertDeliverySettings(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long storeId,
            @Valid @RequestBody StoreDeliverySettingsRequest request) {
        assertAccess(authUser.getId(), storeId);
        return ResponseEntity.ok(settingsService.upsertDeliverySettings(storeId, request));
    }

    @Operation(summary = "Get delivery areas for the merchant's store")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping("/areas")
    public ResponseEntity<List<StoreDeliveryAreaResponse>> getDeliveryAreas(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long storeId) {
        assertAccess(authUser.getId(), storeId);
        return ResponseEntity.ok(settingsService.getDeliveryAreas(storeId));
    }

    @Operation(summary = "Replace all delivery areas for the merchant's store")
    @ApiResponse(responseCode = "200", description = "OK")
    @PutMapping("/areas")
    public ResponseEntity<List<StoreDeliveryAreaResponse>> replaceDeliveryAreas(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long storeId,
            @RequestBody List<@Valid StoreDeliveryAreaRequest> requests) {
        assertAccess(authUser.getId(), storeId);
        return ResponseEntity.ok(settingsService.replaceDeliveryAreas(storeId, requests));
    }

    @Operation(summary = "Get delivery restrictions for the merchant's store")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping("/restrictions")
    public ResponseEntity<List<StoreDeliveryRestrictionResponse>> getDeliveryRestrictions(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long storeId) {
        assertAccess(authUser.getId(), storeId);
        return ResponseEntity.ok(settingsService.getDeliveryRestrictions(storeId));
    }

    @Operation(summary = "Replace all delivery restrictions for the merchant's store")
    @ApiResponse(responseCode = "200", description = "OK")
    @PutMapping("/restrictions")
    public ResponseEntity<List<StoreDeliveryRestrictionResponse>> replaceDeliveryRestrictions(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long storeId,
            @RequestBody List<@Valid StoreDeliveryRestrictionRequest> requests) {
        assertAccess(authUser.getId(), storeId);
        return ResponseEntity.ok(settingsService.replaceDeliveryRestrictions(storeId, requests));
    }

    @Operation(summary = "Get work hours for the merchant's store")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping("/hours")
    public ResponseEntity<List<StoreHoursResponse>> getWorkHours(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long storeId) {
        assertAccess(authUser.getId(), storeId);
        return ResponseEntity.ok(settingsService.getWorkHours(storeId));
    }

    @Operation(summary = "Replace all work hours for the merchant's store")
    @ApiResponse(responseCode = "200", description = "OK")
    @PutMapping("/hours")
    public ResponseEntity<List<StoreHoursResponse>> replaceWorkHours(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long storeId,
            @RequestBody List<@Valid StoreHoursRequest> requests) {
        assertAccess(authUser.getId(), storeId);
        return ResponseEntity.ok(settingsService.replaceWorkHours(storeId, requests));
    }

    @Operation(summary = "Get break schedules for the merchant's store")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping("/breaks")
    public ResponseEntity<List<StoreBreaksResponse>> getBreaks(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long storeId) {
        assertAccess(authUser.getId(), storeId);
        return ResponseEntity.ok(settingsService.getBreaks(storeId));
    }

    @Operation(summary = "Replace all break schedules for the merchant's store")
    @ApiResponse(responseCode = "200", description = "OK")
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