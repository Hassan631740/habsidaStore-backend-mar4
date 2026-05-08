package com.habsida.store.controller.admin;

import com.habsida.store.dto.request.ProductModifierGroupRequest;
import com.habsida.store.dto.response.ProductModifierGroupResponse;
import com.habsida.store.service.ProductModifierGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/** Admin: assign modifier groups to a product (product must belong to store). */
@RestController
@RequestMapping("/api/admin/stores/{storeId}/products/{productId}/modifier-groups")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Modifiers", description = "Modifier groups, options, assign groups to products")
public class AdminStoreProductModifierGroupController {

    private final ProductModifierGroupService productModifierGroupService;

    @GetMapping
    public List<ProductModifierGroupResponse> list(
            @PathVariable Long storeId,
            @PathVariable Long productId,
            Pageable pageable) {
        return productModifierGroupService.listForStore(storeId, productId, pageable);
    }

    @PostMapping
    public ResponseEntity<ProductModifierGroupResponse> assign(
            @PathVariable Long storeId,
            @PathVariable Long productId,
            @Valid @RequestBody ProductModifierGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productModifierGroupService.assignForStore(storeId, productId, request));
    }

    @DeleteMapping("/{modifierGroupId}")
    public ResponseEntity<Void> unassign(
            @PathVariable Long storeId,
            @PathVariable Long productId,
            @PathVariable Long modifierGroupId) {
        productModifierGroupService.unassignForStore(storeId, productId, modifierGroupId);
        return ResponseEntity.noContent().build();
    }
}