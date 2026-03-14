package com.habsida.store.controller.merchant;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.request.CategoryRequest;
import com.habsida.store.dto.response.CategoryResponse;
import com.habsida.store.entity.Category;
import com.habsida.store.exception.ResourceNotFoundException;
import com.habsida.store.repository.CategoryRepository;
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

import java.util.List;

/**
 * Merchant: categories for own store(s) only. GET/POST/PUT/DELETE /api/merchant/categories
 */
@RestController
@RequestMapping("/api/merchant/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
public class MerchantCategoryController {

    private final CategoryRepository categoryRepository;
    private final UserStoreAccessRepository userStoreAccessRepository;

    private List<Long> getMerchantStoreIds(Long userId) {
        return userStoreAccessRepository.findByUserId(userId).stream()
                .map(usa -> usa.getStoreId())
                .filter(id -> id != null)
                .distinct()
                .toList();
    }

    @GetMapping
    public PageResponse<CategoryResponse> findAll(
            @AuthenticationPrincipal AuthUser authUser,
            Pageable pageable) {
        List<Long> storeIds = getMerchantStoreIds(authUser.getId());
        if (storeIds.isEmpty()) {
            return PageResponse.of(org.springframework.data.domain.Page.empty(pageable));
        }
        return PageResponse.of(
                categoryRepository.findByStoreIdIn(storeIds, pageable).map(DtoMapper::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> findById(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id) {
        List<Long> storeIds = getMerchantStoreIds(authUser.getId());
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        if (cat.getStoreId() == null || !storeIds.contains(cat.getStoreId())) {
            throw new ResourceNotFoundException("Category", id);
        }
        return ResponseEntity.ok(DtoMapper.toResponse(cat));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody CategoryRequest request) {
        List<Long> storeIds = getMerchantStoreIds(authUser.getId());
        Long storeId = request.getStoreId();
        if (storeId == null || !storeIds.contains(storeId)) {
            throw new ResourceNotFoundException("Store", storeId);
        }
        Category entity = DtoMapper.toEntity(request);
        entity.setStoreId(storeId);
        Category saved = categoryRepository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        List<Long> storeIds = getMerchantStoreIds(authUser.getId());
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        if (existing.getStoreId() == null || !storeIds.contains(existing.getStoreId())) {
            throw new ResourceNotFoundException("Category", id);
        }
        Long storeId = request.getStoreId() != null ? request.getStoreId() : existing.getStoreId();
        if (!storeIds.contains(storeId)) {
            throw new ResourceNotFoundException("Store", storeId);
        }
        Category entity = DtoMapper.toEntity(request);
        entity.setId(id);
        entity.setStoreId(storeId);
        return ResponseEntity.ok(DtoMapper.toResponse(categoryRepository.save(entity)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id) {
        List<Long> storeIds = getMerchantStoreIds(authUser.getId());
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        if (existing.getStoreId() == null || !storeIds.contains(existing.getStoreId())) {
            throw new ResourceNotFoundException("Category", id);
        }
        categoryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
