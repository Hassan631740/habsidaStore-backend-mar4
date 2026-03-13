package com.habsida.store.controller;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.request.StoreBreaksRequest;
import com.habsida.store.dto.response.StoreBreaksResponse;
import com.habsida.store.entity.StoreBreaks;
import com.habsida.store.repository.StoreBreaksRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/store-breaks")
@RequiredArgsConstructor
public class StoreBreaksController {

    private final StoreBreaksRepository repository;

    @GetMapping
    public PageResponse<StoreBreaksResponse> findAll(Pageable pageable) {
        return PageResponse.of(repository.findAll(pageable).map(DtoMapper::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreBreaksResponse> findById(@PathVariable Long id) {
        return repository.findById(id)
                .map(DtoMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StoreBreaksResponse> create(@Valid @RequestBody StoreBreaksRequest request) {
        StoreBreaks entity = DtoMapper.toEntity(request);
        StoreBreaks saved = repository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StoreBreaksResponse> update(@PathVariable Long id, @Valid @RequestBody StoreBreaksRequest request) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        StoreBreaks entity = DtoMapper.toEntity(request);
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
