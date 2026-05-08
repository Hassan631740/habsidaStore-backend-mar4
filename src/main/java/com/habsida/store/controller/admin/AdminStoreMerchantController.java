package com.habsida.store.controller.admin;

import com.habsida.store.dto.request.AssignMerchantRequest;
import com.habsida.store.dto.response.UserStoreAccessResponse;
import com.habsida.store.service.UserStoreAccessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/stores/{storeId}/merchants")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Stores", description = "Store management (admin: any store; merchant: assigned stores)")
public class AdminStoreMerchantController {

    private final UserStoreAccessService userStoreAccessService;

    @Operation(summary = "List merchant users assigned to a store")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping
    public List<UserStoreAccessResponse> listAssigned(@PathVariable Long storeId) {
        return userStoreAccessService.listByStore(storeId);
    }

    @Operation(summary = "Assign a merchant user to a store")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Assigned"),
        @ApiResponse(responseCode = "404", description = "Store or user not found")
    })
    @PostMapping
    public ResponseEntity<UserStoreAccessResponse> assignMerchant(
            @PathVariable Long storeId,
            @Valid @RequestBody AssignMerchantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userStoreAccessService.assignMerchant(storeId, request));
    }

    @Operation(summary = "Remove a merchant user from a store")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Removed"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> unassignMerchant(@PathVariable Long storeId, @PathVariable Long userId) {
        userStoreAccessService.unassignMerchant(storeId, userId);
        return ResponseEntity.noContent().build();
    }
}