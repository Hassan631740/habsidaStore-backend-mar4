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
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/admin/stores/{storeId}/modifier-groups")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Modifiers", description = "Modifier groups, options, assign groups to products")
public class AdminStoreModifierGroupController {

    private final ModifierGroupService modifierGroupService;

    @GetMapping
    public PageResponse<ModifierGroupResponse> findAll(@PathVariable Long storeId, Pageable pageable) {
        return modifierGroupService.findByStoreId(storeId, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModifierGroupResponse> findById(@PathVariable Long storeId, @PathVariable Long id) {
        return ResponseEntity.ok(modifierGroupService.getByIdForStore(storeId, id));
    }

    @PostMapping
    public ResponseEntity<ModifierGroupResponse> create(
            @PathVariable Long storeId,
            @Valid @RequestBody ModifierGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(modifierGroupService.createForStore(storeId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModifierGroupResponse> update(
            @PathVariable Long storeId,
            @PathVariable Long id,
            @Valid @RequestBody ModifierGroupRequest request) {
        return ResponseEntity.ok(modifierGroupService.updateForStore(storeId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long storeId, @PathVariable Long id) {
        modifierGroupService.deleteForStore(storeId, id);
        return ResponseEntity.noContent().build();
    }
}