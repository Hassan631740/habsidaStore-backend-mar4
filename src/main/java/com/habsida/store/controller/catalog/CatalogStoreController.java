package com.habsida.store.controller.catalog;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.response.StoreResponse;
import com.habsida.store.repository.StoreRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public read-only store browsing. No authentication required.
 */
@RestController
@RequestMapping("/api/catalog/stores")
@RequiredArgsConstructor
@Tag(name = "Catalog", description = "Public read-only store and product browsing (no auth required)")
public class CatalogStoreController {

    private final StoreRepository repository;

    @Operation(summary = "List all active stores")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping
    public PageResponse<StoreResponse> findAll(Pageable pageable) {
        return PageResponse.of(repository.findAll(pageable).map(DtoMapper::toResponse));
    }

    @Operation(summary = "Get a store by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<StoreResponse> findById(@PathVariable Long id) {
        return repository.findById(id)
                .map(DtoMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}