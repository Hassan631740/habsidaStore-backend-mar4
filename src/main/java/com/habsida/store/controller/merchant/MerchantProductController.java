package com.habsida.store.controller.merchant;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.ProductOrderingPausedRequest;
import com.habsida.store.dto.request.ProductRequest;
import com.habsida.store.dto.response.ProductResponse;
import com.habsida.store.security.AuthUser;
import com.habsida.store.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchant/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
@Tag(name = "Products", description = "Product CRUD, filters, pause ordering")
public class MerchantProductController {

    private final ProductService productService;

    @Operation(summary = "List products for the merchant's stores (filterable by ?categoryId=, ?availableForOrder=, ?q=)")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping
    public PageResponse<ProductResponse> findAll(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean availableForOrder,
            @RequestParam(required = false) String q,
            Pageable pageable) {
        return productService.findAllForMerchantUser(authUser.getId(), categoryId, availableForOrder, q, pageable);
    }

    @Operation(summary = "Get a product by ID (must belong to the merchant's store)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id) {
        return productService.findByIdForMerchant(authUser.getId(), id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a product in the merchant's store")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping
    public ResponseEntity<ProductResponse> create(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createForMerchant(authUser.getId(), request));
    }

    @Operation(summary = "Update a product in the merchant's store")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Updated"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return productService.updateForMerchant(authUser.getId(), id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Pause or resume ordering for a product")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Updated"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PatchMapping("/{id}/ordering-paused")
    public ResponseEntity<ProductResponse> setOrderingPaused(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id,
            @Valid @RequestBody ProductOrderingPausedRequest request) {
        return productService.setOrderingPausedForMerchant(authUser.getId(), id, request.getPaused())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete a product from the merchant's store")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Deleted"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id) {
        return productService.deleteByIdForMerchant(authUser.getId(), id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}