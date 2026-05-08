package com.habsida.store.controller.admin;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.ModifierGroupRequest;
import com.habsida.store.dto.response.ModifierGroupResponse;
import com.habsida.store.service.ModifierGroupService;
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
@RequestMapping("/api/admin/stores/{storeId}/modifier-groups")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Modifiers", description = "Modifier groups, options, assign groups to products")
public class AdminStoreModifierGroupController {

    private final ModifierGroupService modifierGroupService;

    @Operation(summary = "List modifier groups for a store")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping
    public PageResponse<ModifierGroupResponse> findAll(@PathVariable Long storeId, Pageable pageable) {
        return modifierGroupService.findByStoreId(storeId, pageable);
    }

    @Operation(summary = "Get a modifier group by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ModifierGroupResponse> findById(@PathVariable Long storeId, @PathVariable Long id) {
        return ResponseEntity.ok(modifierGroupService.getByIdForStore(storeId, id));
    }

    @Operation(summary = "Create a modifier group")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping
    public ResponseEntity<ModifierGroupResponse> create(
            @PathVariable Long storeId,
            @Valid @RequestBody ModifierGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(modifierGroupService.createForStore(storeId, request));
    }

    @Operation(summary = "Update a modifier group")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Updated"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ModifierGroupResponse> update(
            @PathVariable Long storeId,
            @PathVariable Long id,
            @Valid @RequestBody ModifierGroupRequest request) {
        return ResponseEntity.ok(modifierGroupService.updateForStore(storeId, id, request));
    }

    @Operation(summary = "Delete a modifier group")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Deleted"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long storeId, @PathVariable Long id) {
        modifierGroupService.deleteForStore(storeId, id);
        return ResponseEntity.noContent().build();
    }
}