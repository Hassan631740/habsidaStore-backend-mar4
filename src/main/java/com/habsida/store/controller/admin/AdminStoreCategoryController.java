package com.habsida.store.controller.admin;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.CategoryRequest;
import com.habsida.store.dto.response.CategoryResponse;
import com.habsida.store.service.CategoryService;
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

    private final CategoryService categoryService;

    @GetMapping
    public PageResponse<CategoryResponse> findAll(@PathVariable Long storeId, Pageable pageable) {
        return categoryService.findByStoreId(storeId, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> findById(@PathVariable Long storeId, @PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getByIdForStore(storeId, id));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(
            @PathVariable Long storeId,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createForStore(storeId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(
            @PathVariable Long storeId,
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateForStore(storeId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long storeId, @PathVariable Long id) {
        categoryService.deleteForStore(storeId, id);
        return ResponseEntity.noContent().build();
    }
}