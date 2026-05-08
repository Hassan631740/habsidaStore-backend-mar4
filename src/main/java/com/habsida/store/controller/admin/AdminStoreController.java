package com.habsida.store.controller.admin;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.AssignMerchantRequest;
import com.habsida.store.dto.request.StoreRequest;
import com.habsida.store.dto.request.StoreStatusUpdateRequest;
import com.habsida.store.dto.response.StoreResponse;
import com.habsida.store.dto.response.UserStoreAccessResponse;
import com.habsida.store.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Admin-only store management. Requires ROLE_ADMIN.
 */
@RestController
@RequestMapping("/api/admin/stores")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Stores", description = "Store management (admin: any store; merchant: assigned stores)")
public class AdminStoreController {

    private final StoreService storeService;

    @GetMapping
    public PageResponse<StoreResponse> findAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String location,
            Pageable pageable) {
        return storeService.findAll(status, location, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(storeService.getById(id));
    }

    @PostMapping
    public ResponseEntity<StoreResponse> create(@Valid @RequestBody StoreRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(storeService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StoreResponse> update(@PathVariable Long id, @Valid @RequestBody StoreRequest request) {
        return ResponseEntity.ok(storeService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<StoreResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StoreStatusUpdateRequest request) {
        return ResponseEntity.ok(storeService.updateStatus(id, request.getStatus()));
    }

    @PostMapping("/{id}/users")
    public ResponseEntity<UserStoreAccessResponse> assignUser(
            @PathVariable Long id,
            @Valid @RequestBody AssignMerchantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(storeService.assignUserAccess(id, request.getUserId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        storeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}