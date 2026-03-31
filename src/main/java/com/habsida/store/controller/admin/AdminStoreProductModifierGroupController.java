package com.habsida.store.controller.admin;

import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.request.ProductModifierGroupRequest;
import com.habsida.store.dto.response.ProductModifierGroupResponse;
import com.habsida.store.entity.ModifierGroup;
import com.habsida.store.entity.Product;
import com.habsida.store.entity.ProductModifierGroup;
import com.habsida.store.exception.ResourceNotFoundException;
import com.habsida.store.repository.ModifierGroupRepository;
import com.habsida.store.repository.ProductModifierGroupRepository;
import com.habsida.store.repository.ProductRepository;
import com.habsida.store.repository.StoreRepository;
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

    private final ProductModifierGroupRepository repository;
    private final ProductRepository productRepository;
    private final ModifierGroupRepository modifierGroupRepository;
    private final StoreRepository storeRepository;

    private void ensureProductBelongsToStore(Long storeId, Long productId) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", storeId);
        }
        Product p = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        if (!storeId.equals(p.getStoreId())) {
            throw new ResourceNotFoundException("Product", productId);
        }
    }

    private void ensureGroupBelongsToStore(Long storeId, Long groupId) {
        ModifierGroup g = modifierGroupRepository.findById(groupId).orElseThrow(() -> new ResourceNotFoundException("ModifierGroup", groupId));
        if (!storeId.equals(g.getStoreId())) {
            throw new ResourceNotFoundException("ModifierGroup", groupId);
        }
    }

    @GetMapping
    public List<ProductModifierGroupResponse> list(
            @PathVariable Long storeId,
            @PathVariable Long productId,
            Pageable pageable) {
        ensureProductBelongsToStore(storeId, productId);
        return repository.findByProductId(productId, pageable).stream()
                .map(DtoMapper::toResponse)
                .toList();
    }

    @PostMapping
    public ResponseEntity<ProductModifierGroupResponse> assign(
            @PathVariable Long storeId,
            @PathVariable Long productId,
            @Valid @RequestBody ProductModifierGroupRequest request) {
        ensureProductBelongsToStore(storeId, productId);
        ensureGroupBelongsToStore(storeId, request.getModifierGroupId());
        if (!productId.equals(request.getProductId())) {
            throw new IllegalArgumentException("Product ID must match path");
        }
        if (repository.existsByProductIdAndModifierGroupId(productId, request.getModifierGroupId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        ProductModifierGroup entity = DtoMapper.toEntity(request);
        entity.setProductId(productId);
        return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.toResponse(repository.save(entity)));
    }

    @DeleteMapping("/{modifierGroupId}")
    public ResponseEntity<Void> unassign(
            @PathVariable Long storeId,
            @PathVariable Long productId,
            @PathVariable Long modifierGroupId) {
        ensureProductBelongsToStore(storeId, productId);
        ensureGroupBelongsToStore(storeId, modifierGroupId);
        List<ProductModifierGroup> list = repository.findByProductId(productId).stream()
                .filter(pmg -> modifierGroupId.equals(pmg.getModifierGroupId()))
                .toList();
        if (list.isEmpty()) {
            throw new ResourceNotFoundException("Modifier group not assigned to this product");
        }
        list.forEach(repository::delete);
        return ResponseEntity.noContent().build();
    }
}
