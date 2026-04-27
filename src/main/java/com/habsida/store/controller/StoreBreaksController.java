package com.habsida.store.controller;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.StoreBreaksRequest;
import com.habsida.store.dto.response.StoreBreaksResponse;
import com.habsida.store.service.StoreBreaksService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/store-breaks")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class StoreBreaksController {

    private final StoreBreaksService service;

    @GetMapping
    public PageResponse<StoreBreaksResponse> findAll(Pageable pageable) {
        return service.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreBreaksResponse> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StoreBreaksResponse> create(@Valid @RequestBody StoreBreaksRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StoreBreaksResponse> update(@PathVariable Long id, @Valid @RequestBody StoreBreaksRequest request) {
        return service.update(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}