package com.habsida.store.controller;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.request.UserStoreAccessRequest;
import com.habsida.store.dto.response.UserStoreAccessResponse;
import com.habsida.store.entity.UserStoreAccess;
import com.habsida.store.repository.UserStoreAccessRepository;
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

    private final UserStoreAccessRepository repository;

    @GetMapping
    public PageResponse<UserStoreAccessResponse> findAll(Pageable pageable) {
        return PageResponse.of(repository.findAll(pageable).map(DtoMapper::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserStoreAccessResponse> findById(@PathVariable Long id) {
        return repository.findById(id)
                .map(DtoMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UserStoreAccessResponse> create(@Valid @RequestBody UserStoreAccessRequest request) {
        UserStoreAccess entity = DtoMapper.toEntity(request);
        UserStoreAccess saved = repository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserStoreAccessResponse> update(@PathVariable Long id, @Valid @RequestBody UserStoreAccessRequest request) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        UserStoreAccess entity = DtoMapper.toEntity(request);
        entity.setId(id);
        return ResponseEntity.ok(DtoMapper.toResponse(repository.save(entity)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
