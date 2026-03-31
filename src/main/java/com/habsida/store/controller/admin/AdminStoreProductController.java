package com.habsida.store.controller.admin;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.ProductRequest;
import com.habsida.store.dto.response.ProductResponse;
import com.habsida.store.exception.ResourceNotFoundException;
import com.habsida.store.repository.StoreRepository;
import com.habsida.store.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/stores/{storeId}/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Products", description = "Product CRUD, filters, pause ordering")
public class AdminStoreProductController {

    private final ProductService productService;
    private final StoreRepository storeRepository;

    @GetMapping
    public PageResponse<ProductResponse> findAll(
            @PathVariable Long storeId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean availableForOrder,
            @RequestParam(required = false) String q,
            Pageable pageable) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", storeId);
        }
        Map<String, String> filter = new HashMap<>();
        filter.put("storeId", String.valueOf(storeId));
        if (categoryId != null) filter.put("categoryId", String.valueOf(categoryId));
        if (availableForOrder != null) filter.put("availableForOrder", String.valueOf(availableForOrder));
        if (q != null && !q.isBlank()) filter.put("q", q);
        return productService.findAll(pageable, filter);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(@PathVariable Long storeId, @PathVariable Long id) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", storeId);
        }
        return productService.findById(id)
                .filter(p -> storeId.equals(p.getStoreId()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(
            @PathVariable Long storeId,
            @Valid @RequestBody ProductRequest request) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", storeId);
        }
        if (request.getStoreId() != null && !request.getStoreId().equals(storeId)) {
            throw new IllegalArgumentException("Store ID must match path");
        }
        ProductRequest withStore = ProductRequest.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .categoryId(request.getCategoryId())
                .storeId(storeId)
                .availableForOrder(request.getAvailableForOrder() != null ? request.getAvailableForOrder() : true)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(withStore));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long storeId,
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", storeId);
        }
        if (request.getStoreId() != null && !request.getStoreId().equals(storeId)) {
            throw new IllegalArgumentException("Store ID must match path");
        }
        ProductRequest withStore = ProductRequest.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .categoryId(request.getCategoryId())
                .storeId(storeId)
                .availableForOrder(request.getAvailableForOrder() != null ? request.getAvailableForOrder() : true)
                .build();
        return productService.update(id, withStore)
                .filter(p -> storeId.equals(p.getStoreId()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long storeId, @PathVariable Long id) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", storeId);
        }
        var opt = productService.findById(id).filter(p -> storeId.equals(p.getStoreId()));
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        productService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
