package com.habsida.store.service;

import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.ProductModifierGroupRequest;
import com.habsida.store.dto.response.ProductModifierGroupResponse;
import com.habsida.store.entity.Product;
import com.habsida.store.entity.ProductModifierGroup;
import com.habsida.store.entity.UserStoreAccess;
import com.habsida.store.exception.ResourceNotFoundException;
import com.habsida.store.repository.ModifierGroupRepository;
import com.habsida.store.repository.ProductModifierGroupRepository;
import com.habsida.store.repository.ProductRepository;
import com.habsida.store.repository.StoreRepository;
import com.habsida.store.repository.UserStoreAccessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProductModifierGroupService {

    private final ProductModifierGroupRepository repository;
    private final ProductRepository productRepository;
    private final ModifierGroupRepository modifierGroupRepository;
    private final StoreRepository storeRepository;
    private final UserStoreAccessRepository userStoreAccessRepository;

    // --- Basic CRUD (used by ProductModifierGroupController) ---

    @Transactional(readOnly = true)
    public PageResponse<ProductModifierGroupResponse> findAll(Pageable pageable) {
        return PageResponse.of(repository.findAll(pageable).map(DtoMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public Optional<ProductModifierGroupResponse> findById(Long id) {
        return repository.findById(id).map(DtoMapper::toResponse);
    }

    @Transactional
    public ProductModifierGroupResponse create(ProductModifierGroupRequest request) {
        ProductModifierGroup entity = DtoMapper.toEntity(request);
        return DtoMapper.toResponse(repository.save(entity));
    }

    @Transactional
    public Optional<ProductModifierGroupResponse> update(Long id, ProductModifierGroupRequest request) {
        if (!repository.existsById(id)) {
            return Optional.empty();
        }
        ProductModifierGroup entity = DtoMapper.toEntity(request);
        entity.setId(id);
        return Optional.of(DtoMapper.toResponse(repository.save(entity)));
    }

    @Transactional
    public boolean delete(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }

    // --- Admin (store-scoped) operations ---

    @Transactional(readOnly = true)
    public List<ProductModifierGroupResponse> listForStore(Long storeId, Long productId, Pageable pageable) {
        ensureProductBelongsToStore(storeId, productId);
        return repository.findByProductId(productId, pageable).stream()
                .map(DtoMapper::toResponse)
                .toList();
    }

    @Transactional
    public ProductModifierGroupResponse assignForStore(Long storeId, Long productId, ProductModifierGroupRequest request) {
        ensureProductBelongsToStore(storeId, productId);
        ensureGroupBelongsToStore(storeId, request.getModifierGroupId());
        if (!productId.equals(request.getProductId())) {
            throw new IllegalArgumentException("Product ID must match path");
        }
        if (repository.existsByProductIdAndModifierGroupId(productId, request.getModifierGroupId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Modifier group already assigned to product");
        }
        ProductModifierGroup entity = DtoMapper.toEntity(request);
        entity.setProductId(productId);
        return DtoMapper.toResponse(repository.save(entity));
    }

    @Transactional
    public void unassignForStore(Long storeId, Long productId, Long modifierGroupId) {
        ensureProductBelongsToStore(storeId, productId);
        ensureGroupBelongsToStore(storeId, modifierGroupId);
        List<ProductModifierGroup> list = repository.findByProductId(productId).stream()
                .filter(pmg -> modifierGroupId.equals(pmg.getModifierGroupId()))
                .toList();
        if (list.isEmpty()) {
            throw new ResourceNotFoundException("Modifier group not assigned to this product");
        }
        list.forEach(repository::delete);
    }

    // --- Merchant (user-scoped) operations ---

    @Transactional(readOnly = true)
    public List<ProductModifierGroupResponse> listForMerchant(Long userId, Long productId, Pageable pageable) {
        ensureProductBelongsToMerchant(userId, productId);
        return repository.findByProductId(productId, pageable).stream()
                .map(DtoMapper::toResponse)
                .toList();
    }

    @Transactional
    public ProductModifierGroupResponse assignForMerchant(Long userId, Long productId, ProductModifierGroupRequest request) {
        ensureProductBelongsToMerchant(userId, productId);
        ensureGroupBelongsToMerchant(userId, request.getModifierGroupId());
        if (!productId.equals(request.getProductId())) {
            throw new IllegalArgumentException("Product ID must match path");
        }
        if (repository.existsByProductIdAndModifierGroupId(productId, request.getModifierGroupId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Modifier group already assigned to product");
        }
        ProductModifierGroup entity = DtoMapper.toEntity(request);
        entity.setProductId(productId);
        return DtoMapper.toResponse(repository.save(entity));
    }

    @Transactional
    public void unassignForMerchant(Long userId, Long productId, Long modifierGroupId) {
        ensureProductBelongsToMerchant(userId, productId);
        ensureGroupBelongsToMerchant(userId, modifierGroupId);
        List<ProductModifierGroup> list = repository.findByProductId(productId).stream()
                .filter(pmg -> modifierGroupId.equals(pmg.getModifierGroupId()))
                .toList();
        if (list.isEmpty()) {
            throw new ResourceNotFoundException("Modifier group not assigned to this product");
        }
        list.forEach(repository::delete);
    }

    private void ensureProductBelongsToStore(Long storeId, Long productId) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", storeId);
        }
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        if (!storeId.equals(p.getStoreId())) {
            throw new ResourceNotFoundException("Product", productId);
        }
    }

    private void ensureGroupBelongsToStore(Long storeId, Long groupId) {
        var g = modifierGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("ModifierGroup", groupId));
        if (!storeId.equals(g.getStoreId())) {
            throw new ResourceNotFoundException("ModifierGroup", groupId);
        }
    }

    private void ensureProductBelongsToMerchant(Long userId, Long productId) {
        List<Long> storeIds = getMerchantStoreIds(userId);
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        if (!storeIds.contains(p.getStoreId())) {
            throw new ResourceNotFoundException("Product", productId);
        }
    }

    private void ensureGroupBelongsToMerchant(Long userId, Long groupId) {
        List<Long> storeIds = getMerchantStoreIds(userId);
        var g = modifierGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("ModifierGroup", groupId));
        if (!storeIds.contains(g.getStoreId())) {
            throw new ResourceNotFoundException("ModifierGroup", groupId);
        }
    }

    private List<Long> getMerchantStoreIds(Long userId) {
        return userStoreAccessRepository.findByUserId(userId).stream()
                .map(UserStoreAccess::getStoreId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
}