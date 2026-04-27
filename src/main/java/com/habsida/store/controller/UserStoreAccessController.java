package com.habsida.store.controller;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.UserStoreAccessRequest;
import com.habsida.store.dto.response.UserStoreAccessResponse;
import com.habsida.store.service.UserStoreAccessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user-store-access")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserStoreAccessController {

    private final UserStoreAccessService service;

    @GetMapping
    public PageResponse<UserStoreAccessResponse> findAll(Pageable pageable) {
        return service.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserStoreAccessResponse> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UserStoreAccessResponse> create(@Valid @RequestBody UserStoreAccessRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserStoreAccessResponse> update(@PathVariable Long id, @Valid @RequestBody UserStoreAccessRequest request) {
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