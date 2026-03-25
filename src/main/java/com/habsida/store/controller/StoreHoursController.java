package com.habsida.store.controller;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.request.StoreHoursRequest;
import com.habsida.store.dto.response.StoreHoursResponse;
import com.habsida.store.entity.StoreHours;
import com.habsida.store.repository.StoreHoursRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/store-hours")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class StoreHoursController {

    private final StoreHoursRepository repository;

    @GetMapping
    public PageResponse<StoreHoursResponse> findAll(Pageable pageable) {
        return PageResponse.of(repository.findAll(pageable).map(DtoMapper::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreHoursResponse> findById(@PathVariable Long id) {
        return repository.findById(id)
                .map(DtoMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StoreHoursResponse> create(@Valid @RequestBody StoreHoursRequest request) {
        StoreHours entity = DtoMapper.toEntity(request);
        StoreHours saved = repository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StoreHoursResponse> update(@PathVariable Long id, @Valid @RequestBody StoreHoursRequest request) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        StoreHours entity = DtoMapper.toEntity(request);
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
