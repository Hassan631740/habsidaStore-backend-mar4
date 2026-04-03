package com.habsida.store.controller.merchant;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.CategoryRequest;
import com.habsida.store.dto.response.CategoryResponse;
import com.habsida.store.security.AuthUser;
import com.habsida.store.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Merchant: categories for own store(s) only. GET/POST/PUT/DELETE /api/merchant/categories
 */
@RestController
@RequestMapping("/api/merchant/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
@Tag(name = "Categories", description = "Category CRUD per store")
public class MerchantCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public PageResponse<CategoryResponse> findAll(
            @AuthenticationPrincipal AuthUser authUser,
            Pageable pageable) {
        return categoryService.findAllForMerchant(authUser.getId(), pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> findById(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getByIdForMerchant(authUser.getId(), id));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.createForMerchant(authUser.getId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateForMerchant(authUser.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id) {
        categoryService.deleteForMerchant(authUser.getId(), id);
        return ResponseEntity.noContent().build();
    }
}