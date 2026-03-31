package com.habsida.store.controller.admin;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.request.CategoryRequest;
import com.habsida.store.dto.response.CategoryResponse;
import com.habsida.store.entity.Category;
import com.habsida.store.exception.ResourceNotFoundException;
import com.habsida.store.repository.CategoryRepository;
import com.habsida.store.repository.StoreRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Admin: categories for a specific store. GET/POST/PUT/DELETE /api/admin/stores/{storeId}/categories
 */
@RestController
@RequestMapping("/api/admin/stores/{storeId}/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Categories", description = "Category CRUD per store")
public class AdminStoreCategoryController {

    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;

    @GetMapping
    public PageResponse<CategoryResponse> findAll(@PathVariable Long storeId, Pageable pageable) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", storeId);
        }
        return PageResponse.of(categoryRepository.findByStoreId(storeId, pageable).map(DtoMapper::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> findById(@PathVariable Long storeId, @PathVariable Long id) {
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        if (cat.getStoreId() == null || !cat.getStoreId().equals(storeId)) {
            throw new ResourceNotFoundException("Category", id);
        }
        return ResponseEntity.ok(DtoMapper.toResponse(cat));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(
            @PathVariable Long storeId,
            @Valid @RequestBody CategoryRequest request) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", storeId);
        }
        if (request.getStoreId() != null && !request.getStoreId().equals(storeId)) {
            throw new IllegalArgumentException("Store ID in path and body must match");
        }
        Category entity = DtoMapper.toEntity(request);
        entity.setStoreId(storeId);
        Category saved = categoryRepository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(
            @PathVariable Long storeId,
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        if (existing.getStoreId() == null || !existing.getStoreId().equals(storeId)) {
            throw new ResourceNotFoundException("Category", id);
        }
        if (request.getStoreId() != null && !request.getStoreId().equals(storeId)) {
            throw new IllegalArgumentException("Store ID in path and body must match");
        }
        Category entity = DtoMapper.toEntity(request);
        entity.setId(id);
        entity.setStoreId(storeId);
        return ResponseEntity.ok(DtoMapper.toResponse(categoryRepository.save(entity)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long storeId, @PathVariable Long id) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        if (existing.getStoreId() == null || !existing.getStoreId().equals(storeId)) {
            throw new ResourceNotFoundException("Category", id);
        }
        categoryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
