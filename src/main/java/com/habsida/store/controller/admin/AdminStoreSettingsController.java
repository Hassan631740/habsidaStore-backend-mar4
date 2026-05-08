package com.habsida.store.controller.admin;

import com.habsida.store.dto.request.*;
import com.habsida.store.dto.response.*;
import com.habsida.store.service.StoreSettingsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin endpoints for store settings: delivery, areas, restrictions, hours, breaks.
 * All operations require ROLE_ADMIN.
 */
@RestController
@RequestMapping("/api/admin/stores/{storeId}/settings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Store Settings (Admin)", description = "Admin: manage delivery, hours, and breaks for any store")
public class AdminStoreSettingsController {

    private final StoreSettingsService settingsService;

    // ---- Delivery Settings ----

    @GetMapping("/delivery")
    public ResponseEntity<StoreDeliverySettingsResponse> getDeliverySettings(@PathVariable Long storeId) {
        return ResponseEntity.ok(settingsService.getDeliverySettings(storeId));
    }

    @PutMapping("/delivery")
    public ResponseEntity<StoreDeliverySettingsResponse> upsertDeliverySettings(
            @PathVariable Long storeId,
            @Valid @RequestBody StoreDeliverySettingsRequest request) {
        return ResponseEntity.ok(settingsService.upsertDeliverySettings(storeId, request));
    }

    // ---- Delivery Areas ----

    @GetMapping("/areas")
    public ResponseEntity<List<StoreDeliveryAreaResponse>> getDeliveryAreas(@PathVariable Long storeId) {
        return ResponseEntity.ok(settingsService.getDeliveryAreas(storeId));
    }

    @PutMapping("/areas")
    public ResponseEntity<List<StoreDeliveryAreaResponse>> replaceDeliveryAreas(
            @PathVariable Long storeId,
            @RequestBody List<@Valid StoreDeliveryAreaRequest> requests) {
        return ResponseEntity.ok(settingsService.replaceDeliveryAreas(storeId, requests));
    }

    // ---- Delivery Restrictions ----

    @GetMapping("/restrictions")
    public ResponseEntity<List<StoreDeliveryRestrictionResponse>> getDeliveryRestrictions(@PathVariable Long storeId) {
        return ResponseEntity.ok(settingsService.getDeliveryRestrictions(storeId));
    }

    @PutMapping("/restrictions")
    public ResponseEntity<List<StoreDeliveryRestrictionResponse>> replaceDeliveryRestrictions(
            @PathVariable Long storeId,
            @RequestBody List<@Valid StoreDeliveryRestrictionRequest> requests) {
        return ResponseEntity.ok(settingsService.replaceDeliveryRestrictions(storeId, requests));
    }

    // ---- Work Hours ----

    @GetMapping("/hours")
    public ResponseEntity<List<StoreHoursResponse>> getWorkHours(@PathVariable Long storeId) {
        return ResponseEntity.ok(settingsService.getWorkHours(storeId));
    }

    @PutMapping("/hours")
    public ResponseEntity<List<StoreHoursResponse>> replaceWorkHours(
            @PathVariable Long storeId,
            @RequestBody List<@Valid StoreHoursRequest> requests) {
        return ResponseEntity.ok(settingsService.replaceWorkHours(storeId, requests));
    }

    // ---- Breaks ----

    @GetMapping("/breaks")
    public ResponseEntity<List<StoreBreaksResponse>> getBreaks(@PathVariable Long storeId) {
        return ResponseEntity.ok(settingsService.getBreaks(storeId));
    }

    @PutMapping("/breaks")
    public ResponseEntity<List<StoreBreaksResponse>> replaceBreaks(
            @PathVariable Long storeId,
            @RequestBody List<@Valid StoreBreaksRequest> requests) {
        return ResponseEntity.ok(settingsService.replaceBreaks(storeId, requests));
    }
}