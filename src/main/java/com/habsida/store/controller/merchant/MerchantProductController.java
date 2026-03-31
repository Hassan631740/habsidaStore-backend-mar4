package com.habsida.store.controller.merchant;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.ProductOrderingPausedRequest;
import com.habsida.store.dto.request.ProductRequest;
import com.habsida.store.dto.response.ProductResponse;
import com.habsida.store.exception.ResourceNotFoundException;
import com.habsida.store.repository.UserStoreAccessRepository;
import com.habsida.store.security.AuthUser;
import com.habsida.store.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/merchant/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
@Tag(name = "Products", description = "Product CRUD, filters, pause ordering")
public class MerchantProductController {

    private final ProductService productService;
    private final UserStoreAccessRepository userStoreAccessRepository;

    private List<Long> getMerchantStoreIds(Long userId) {
        return userStoreAccessRepository.findByUserId(userId).stream()
                .map(usa -> usa.getStoreId())
                .filter(id -> id != null)
                .distinct()
                .toList();
    }

    @GetMapping
    public PageResponse<ProductResponse> findAll(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean availableForOrder,
            @RequestParam(required = false) String q,
            Pageable pageable) {
        List<Long> storeIds = getMerchantStoreIds(authUser.getId());
        Map<String, String> filter = new HashMap<>();
        if (categoryId != null) filter.put("categoryId", String.valueOf(categoryId));
        if (availableForOrder != null) filter.put("availableForOrder", String.valueOf(availableForOrder));
        if (q != null && !q.isBlank()) filter.put("q", q);
        return productService.findAllForStores(storeIds, pageable, filter);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        List<Long> storeIds = getMerchantStoreIds(authUser.getId());
        return productService.findById(id)
                .filter(p -> p.getStoreId() != null && storeIds.contains(p.getStoreId()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody ProductRequest request) {
        List<Long> storeIds = getMerchantStoreIds(authUser.getId());
        if (request.getStoreId() == null || !storeIds.contains(request.getStoreId())) {
            throw new ResourceNotFoundException("Store", request.getStoreId());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        List<Long> storeIds = getMerchantStoreIds(authUser.getId());
        if (request.getStoreId() != null && !storeIds.contains(request.getStoreId())) {
            throw new ResourceNotFoundException("Store", request.getStoreId());
        }
        return productService.findById(id)
                .filter(p -> storeIds.contains(p.getStoreId()))
                .map(p -> productService.update(id, request).orElse(null))
                .filter(r -> r != null)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/ordering-paused")
    public ResponseEntity<ProductResponse> setOrderingPaused(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id,
            @Valid @RequestBody ProductOrderingPausedRequest request) {
        List<Long> storeIds = getMerchantStoreIds(authUser.getId());
        return productService.findById(id)
                .filter(p -> storeIds.contains(p.getStoreId()))
                .map(p -> productService.setOrderingPaused(id, request.getPaused()).orElse(null))
                .filter(r -> r != null)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        List<Long> storeIds = getMerchantStoreIds(authUser.getId());
        var opt = productService.findById(id).filter(p -> storeIds.contains(p.getStoreId()));
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        productService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
