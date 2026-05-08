package com.habsida.store.controller.admin;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.ModifierOptionRequest;
import com.habsida.store.dto.response.ModifierOptionResponse;
import com.habsida.store.service.ModifierOptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/admin/stores/{storeId}/modifier-groups/{groupId}/options")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Modifiers", description = "Modifier groups, options, assign groups to products")
public class AdminStoreModifierOptionController {

    private final ModifierOptionService modifierOptionService;

    @GetMapping
    public PageResponse<ModifierOptionResponse> findAll(
            @PathVariable Long storeId,
            @PathVariable Long groupId,
            Pageable pageable) {
        return modifierOptionService.findByGroupForStore(storeId, groupId, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModifierOptionResponse> findById(
            @PathVariable Long storeId,
            @PathVariable Long groupId,
            @PathVariable Long id) {
        return ResponseEntity.ok(modifierOptionService.getByIdForStore(storeId, groupId, id));
    }

    @PostMapping
    public ResponseEntity<ModifierOptionResponse> create(
            @PathVariable Long storeId,
            @PathVariable Long groupId,
            @Valid @RequestBody ModifierOptionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(modifierOptionService.createForStore(storeId, groupId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModifierOptionResponse> update(
            @PathVariable Long storeId,
            @PathVariable Long groupId,
            @PathVariable Long id,
            @Valid @RequestBody ModifierOptionRequest request) {
        return ResponseEntity.ok(modifierOptionService.updateForStore(storeId, groupId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long storeId,
            @PathVariable Long groupId,
            @PathVariable Long id) {
        modifierOptionService.deleteForStore(storeId, groupId, id);
        return ResponseEntity.noContent().build();
    }
}