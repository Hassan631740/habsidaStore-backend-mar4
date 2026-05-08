package com.habsida.store.controller.merchant;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.ModifierOptionRequest;
import com.habsida.store.dto.response.ModifierOptionResponse;
import com.habsida.store.security.AuthUser;
import com.habsida.store.service.ModifierOptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/merchant/modifier-groups/{groupId}/options")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
@Tag(name = "Modifiers", description = "Modifier groups, options, assign groups to products")
public class MerchantModifierOptionController {

    private final ModifierOptionService modifierOptionService;

    @GetMapping
    public PageResponse<ModifierOptionResponse> findAll(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long groupId,
            Pageable pageable) {
        return modifierOptionService.findByGroupForMerchant(authUser.getId(), groupId, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModifierOptionResponse> findById(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long groupId,
            @PathVariable Long id) {
        return ResponseEntity.ok(modifierOptionService.getByIdForMerchant(authUser.getId(), groupId, id));
    }

    @PostMapping
    public ResponseEntity<ModifierOptionResponse> create(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long groupId,
            @Valid @RequestBody ModifierOptionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(modifierOptionService.createForMerchant(authUser.getId(), groupId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModifierOptionResponse> update(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long groupId,
            @PathVariable Long id,
            @Valid @RequestBody ModifierOptionRequest request) {
        return ResponseEntity.ok(modifierOptionService.updateForMerchant(authUser.getId(), groupId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long groupId,
            @PathVariable Long id) {
        modifierOptionService.deleteForMerchant(authUser.getId(), groupId, id);
        return ResponseEntity.noContent().build();
    }
}