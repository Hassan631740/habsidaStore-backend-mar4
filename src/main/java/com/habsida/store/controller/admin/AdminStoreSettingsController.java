package com.habsida.store.controller.admin;

import com.habsida.store.dto.request.*;
import com.habsida.store.dto.response.*;
import com.habsida.store.service.StoreSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/stores/{storeId}/settings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Store Settings (Admin)", description = "Admin: manage delivery, hours, and breaks for any store")
public class AdminStoreSettingsController {

    private final StoreSettingsService settingsService;

    @Operation(summary = "Get delivery settings for a store")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping("/delivery")
    public ResponseEntity<StoreDeliverySettingsResponse> getDeliverySettings(@PathVariable Long storeId) {
        return ResponseEntity.ok(settingsService.getDeliverySettings(storeId));
    }

    @Operation(summary = "Create or update delivery settings for a store")
    @ApiResponse(responseCode = "200", description = "OK")
    @PutMapping("/delivery")
    public ResponseEntity<StoreDeliverySettingsResponse> upsertDeliverySettings(
            @PathVariable Long storeId,
            @Valid @RequestBody StoreDeliverySettingsRequest request) {
        return ResponseEntity.ok(settingsService.upsertDeliverySettings(storeId, request));
    }

    @Operation(summary = "Get delivery areas for a store")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping("/areas")
    public ResponseEntity<List<StoreDeliveryAreaResponse>> getDeliveryAreas(@PathVariable Long storeId) {
        return ResponseEntity.ok(settingsService.getDeliveryAreas(storeId));
    }

    @Operation(summary = "Replace all delivery areas for a store")
    @ApiResponse(responseCode = "200", description = "OK")
    @PutMapping("/areas")
    public ResponseEntity<List<StoreDeliveryAreaResponse>> replaceDeliveryAreas(
            @PathVariable Long storeId,
            @RequestBody List<@Valid StoreDeliveryAreaRequest> requests) {
        return ResponseEntity.ok(settingsService.replaceDeliveryAreas(storeId, requests));
    }

    @Operation(summary = "Get delivery restrictions for a store")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping("/restrictions")
    public ResponseEntity<List<StoreDeliveryRestrictionResponse>> getDeliveryRestrictions(@PathVariable Long storeId) {
        return ResponseEntity.ok(settingsService.getDeliveryRestrictions(storeId));
    }

    @Operation(summary = "Replace all delivery restrictions for a store")
    @ApiResponse(responseCode = "200", description = "OK")
    @PutMapping("/restrictions")
    public ResponseEntity<List<StoreDeliveryRestrictionResponse>> replaceDeliveryRestrictions(
            @PathVariable Long storeId,
            @RequestBody List<@Valid StoreDeliveryRestrictionRequest> requests) {
        return ResponseEntity.ok(settingsService.replaceDeliveryRestrictions(storeId, requests));
    }

    @Operation(summary = "Get work hours for a store")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping("/hours")
    public ResponseEntity<List<StoreHoursResponse>> getWorkHours(@PathVariable Long storeId) {
        return ResponseEntity.ok(settingsService.getWorkHours(storeId));
    }

    @Operation(summary = "Replace all work hours for a store")
    @ApiResponse(responseCode = "200", description = "OK")
    @PutMapping("/hours")
    public ResponseEntity<List<StoreHoursResponse>> replaceWorkHours(
            @PathVariable Long storeId,
            @RequestBody List<@Valid StoreHoursRequest> requests) {
        return ResponseEntity.ok(settingsService.replaceWorkHours(storeId, requests));
    }

    @Operation(summary = "Get break schedules for a store")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping("/breaks")
    public ResponseEntity<List<StoreBreaksResponse>> getBreaks(@PathVariable Long storeId) {
        return ResponseEntity.ok(settingsService.getBreaks(storeId));
    }

    @Operation(summary = "Replace all break schedules for a store")
    @ApiResponse(responseCode = "200", description = "OK")
    @PutMapping("/breaks")
    public ResponseEntity<List<StoreBreaksResponse>> replaceBreaks(
            @PathVariable Long storeId,
            @RequestBody List<@Valid StoreBreaksRequest> requests) {
        return ResponseEntity.ok(settingsService.replaceBreaks(storeId, requests));
    }
}