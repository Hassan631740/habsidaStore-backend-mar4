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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/stores")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Stores", description = "Store management (admin: any store; merchant: assigned stores)")
public class AdminStoreController {

    private final StoreService storeService;

    @Operation(summary = "List all stores (filterable by ?status=, ?location=)")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping
    public PageResponse<StoreResponse> findAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String location,
            Pageable pageable) {
        return storeService.findAll(status, location, pageable);
    }

    @Operation(summary = "Get a store by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<StoreResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(storeService.getById(id));
    }

    @Operation(summary = "Create a store")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping
    public ResponseEntity<StoreResponse> create(@Valid @RequestBody StoreRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(storeService.create(request));
    }

    @Operation(summary = "Update a store")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Updated"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<StoreResponse> update(@PathVariable Long id, @Valid @RequestBody StoreRequest request) {
        return ResponseEntity.ok(storeService.update(id, request));
    }

    @Operation(summary = "Update store status (ACTIVE / INACTIVE)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Updated"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<StoreResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StoreStatusUpdateRequest request) {
        return ResponseEntity.ok(storeService.updateStatus(id, request.getStatus()));
    }

    @Operation(summary = "Assign a merchant user to a store")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Assigned"),
        @ApiResponse(responseCode = "404", description = "Store or user not found")
    })
    @PostMapping("/{id}/users")
    public ResponseEntity<UserStoreAccessResponse> assignUser(
            @PathVariable Long id,
            @Valid @RequestBody AssignMerchantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(storeService.assignUserAccess(id, request.getUserId()));
    }

    @Operation(summary = "Delete a store")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Deleted"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        storeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}