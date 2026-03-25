package com.habsida.store.controller;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.request.ModifierOptionRequest;
import com.habsida.store.dto.response.ModifierOptionResponse;
import com.habsida.store.entity.ModifierOption;
import com.habsida.store.repository.ModifierOptionRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/modifier-options")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Modifiers", description = "Modifier groups, options, assign to products")
public class ModifierOptionController {

    private final ModifierOptionRepository repository;

    @GetMapping
    public PageResponse<ModifierOptionResponse> findAll(Pageable pageable) {
        return PageResponse.of(repository.findAll(pageable).map(DtoMapper::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModifierOptionResponse> findById(@PathVariable Long id) {
        return repository.findById(id)
                .map(DtoMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ModifierOptionResponse> create(@Valid @RequestBody ModifierOptionRequest request) {
        ModifierOption entity = DtoMapper.toEntity(request);
        ModifierOption saved = repository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModifierOptionResponse> update(@PathVariable Long id, @Valid @RequestBody ModifierOptionRequest request) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        ModifierOption entity = DtoMapper.toEntity(request);
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
