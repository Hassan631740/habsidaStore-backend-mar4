package com.habsida.store.controller.catalog;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.response.StoreResponse;
import com.habsida.store.repository.StoreRepository;
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
public class CatalogStoreController {

    private final StoreRepository repository;

    @GetMapping
    public PageResponse<StoreResponse> findAll(Pageable pageable) {
        return PageResponse.of(repository.findAll(pageable).map(DtoMapper::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreResponse> findById(@PathVariable Long id) {
        return repository.findById(id)
                .map(DtoMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}