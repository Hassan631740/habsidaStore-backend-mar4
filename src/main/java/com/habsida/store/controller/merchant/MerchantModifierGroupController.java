package com.habsida.store.controller.merchant;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.ModifierGroupRequest;
import com.habsida.store.dto.response.ModifierGroupResponse;
import com.habsida.store.security.AuthUser;
import com.habsida.store.service.ModifierGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchant/modifier-groups")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
@Tag(name = "Modifiers", description = "Modifier groups, options, assign groups to products")
public class MerchantModifierGroupController {

    private final ModifierGroupService modifierGroupService;

    @Operation(summary = "List modifier groups for the merchant's stores")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping
    public PageResponse<ModifierGroupResponse> findAll(
            @AuthenticationPrincipal AuthUser authUser,
            Pageable pageable) {
        return modifierGroupService.findAllForMerchant(authUser.getId(), pageable);
    }

    @Operation(summary = "Get a modifier group by ID")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "Not found")})
    @GetMapping("/{id}")
    public ResponseEntity<ModifierGroupResponse> findById(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id) {
        return ResponseEntity.ok(modifierGroupService.getByIdForMerchant(authUser.getId(), id));
    }

    @Operation(summary = "Create a modifier group")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Created"), @ApiResponse(responseCode = "400", description = "Validation error")})
    @PostMapping
    public ResponseEntity<ModifierGroupResponse> create(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody ModifierGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(modifierGroupService.createForMerchant(authUser.getId(), request));
    }

    @Operation(summary = "Update a modifier group")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Updated"), @ApiResponse(responseCode = "404", description = "Not found")})
    @PutMapping("/{id}")
    public ResponseEntity<ModifierGroupResponse> update(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id,
            @Valid @RequestBody ModifierGroupRequest request) {
        return ResponseEntity.ok(modifierGroupService.updateForMerchant(authUser.getId(), id, request));
    }

    @Operation(summary = "Delete a modifier group")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Deleted"), @ApiResponse(responseCode = "404", description = "Not found")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id) {
        modifierGroupService.deleteForMerchant(authUser.getId(), id);
        return ResponseEntity.noContent().build();
    }
}