package com.habsida.store.controller.catalog;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.response.ProductResponse;
import com.habsida.store.exception.ResourceNotFoundException;
import com.habsida.store.repository.ProductRepository;
import com.habsida.store.repository.StoreRepository;
import com.habsida.store.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Public read-only product browsing. No authentication required.
 * Products are always browsed in the context of a store.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Catalog", description = "Public read-only store and product browsing (no auth required)")
public class CatalogProductController {

    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final ProductService productService;

    @Operation(summary = "List products for a store (filterable by ?q=, ?categoryId=, ?availableForOrder=)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Store not found")
    })
    @GetMapping("/api/catalog/stores/{storeId}/products")
    public PageResponse<ProductResponse> findByStore(
            @PathVariable Long storeId,
            @RequestParam(required = false) Map<String, String> filter,
            Pageable pageable) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", storeId);
        }
        return productService.findAllForStores(java.util.List.of(storeId), pageable, filter);
    }

    @Operation(summary = "Get a single product by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/api/catalog/products/{id}")
    public ResponseEntity<ProductResponse> findById(@PathVariable Long id) {
        return productService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
