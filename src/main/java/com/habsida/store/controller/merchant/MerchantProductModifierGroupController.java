package com.habsida.store.controller.merchant;

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
import com.habsida.store.repository.UserStoreAccessRepository;
import com.habsida.store.security.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/merchant/products/{productId}/modifier-groups")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
@Tag(name = "Modifiers", description = "Modifier groups, options, assign groups to products")
public class MerchantProductModifierGroupController {

    private final ProductModifierGroupRepository repository;
    private final ProductRepository productRepository;
    private final ModifierGroupRepository modifierGroupRepository;
    private final UserStoreAccessRepository userStoreAccessRepository;

    private List<Long> getMerchantStoreIds(Long userId) {
        return userStoreAccessRepository.findByUserId(userId).stream()
                .map(usa -> usa.getStoreId())
                .filter(id -> id != null)
                .distinct()
                .toList();
    }

    private void ensureProductBelongsToMerchant(Long userId, Long productId) {
        List<Long> storeIds = getMerchantStoreIds(userId);
        Product p = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        if (!storeIds.contains(p.getStoreId())) {
            throw new ResourceNotFoundException("Product", productId);
        }
    }

    private void ensureGroupBelongsToMerchant(Long userId, Long groupId) {
        List<Long> storeIds = getMerchantStoreIds(userId);
        ModifierGroup g = modifierGroupRepository.findById(groupId).orElseThrow(() -> new ResourceNotFoundException("ModifierGroup", groupId));
        if (!storeIds.contains(g.getStoreId())) {
            throw new ResourceNotFoundException("ModifierGroup", groupId);
        }
    }

    @GetMapping
    public List<ProductModifierGroupResponse> list(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long productId,
            Pageable pageable) {
        ensureProductBelongsToMerchant(authUser.getId(), productId);
        return repository.findByProductId(productId, pageable).stream()
                .map(DtoMapper::toResponse)
                .toList();
    }

    @PostMapping
    public ResponseEntity<ProductModifierGroupResponse> assign(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long productId,
            @Valid @RequestBody ProductModifierGroupRequest request) {
        ensureProductBelongsToMerchant(authUser.getId(), productId);
        ensureGroupBelongsToMerchant(authUser.getId(), request.getModifierGroupId());
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
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long productId,
            @PathVariable Long modifierGroupId) {
        ensureProductBelongsToMerchant(authUser.getId(), productId);
        ensureGroupBelongsToMerchant(authUser.getId(), modifierGroupId);
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
