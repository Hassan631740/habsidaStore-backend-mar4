package com.habsida.store.controller.admin;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.ProductRequest;
import com.habsida.store.dto.response.ProductResponse;
import com.habsida.store.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/admin/stores/{storeId}/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Products", description = "Product CRUD, filters, pause ordering")
public class AdminStoreProductController {

    private final ProductService productService;

    @GetMapping
    public PageResponse<ProductResponse> findAll(
            @PathVariable Long storeId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean availableForOrder,
            @RequestParam(required = false) String q,
            Pageable pageable) {
        return productService.findAllForStore(storeId, categoryId, availableForOrder, q, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(@PathVariable Long storeId, @PathVariable Long id) {
        return productService.findByIdForStore(storeId, id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(
            @PathVariable Long storeId,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createForStore(storeId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long storeId,
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return productService.updateForStore(storeId, id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long storeId, @PathVariable Long id) {
        return productService.deleteByIdForStore(storeId, id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}