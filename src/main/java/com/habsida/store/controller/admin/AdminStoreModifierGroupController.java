package com.habsida.store.controller.admin;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.request.ModifierGroupRequest;
import com.habsida.store.dto.response.ModifierGroupResponse;
import com.habsida.store.entity.ModifierGroup;
import com.habsida.store.exception.ResourceNotFoundException;
import com.habsida.store.repository.ModifierGroupRepository;
import com.habsida.store.repository.StoreRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/admin/stores/{storeId}/modifier-groups")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Modifiers", description = "Modifier groups, options, assign groups to products")
public class AdminStoreModifierGroupController {

    private final ModifierGroupRepository repository;
    private final StoreRepository storeRepository;

    @GetMapping
    public PageResponse<ModifierGroupResponse> findAll(@PathVariable Long storeId, Pageable pageable) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", storeId);
        }
        return PageResponse.of(repository.findByStoreId(storeId, pageable).map(DtoMapper::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModifierGroupResponse> findById(@PathVariable Long storeId, @PathVariable Long id) {
        ModifierGroup g = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("ModifierGroup", id));
        if (!storeId.equals(g.getStoreId())) {
            throw new ResourceNotFoundException("ModifierGroup", id);
        }
        return ResponseEntity.ok(DtoMapper.toResponse(g));
    }

    @PostMapping
    public ResponseEntity<ModifierGroupResponse> create(
            @PathVariable Long storeId,
            @Valid @RequestBody ModifierGroupRequest request) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", storeId);
        }
        if (request.getStoreId() != null && !request.getStoreId().equals(storeId)) {
            throw new IllegalArgumentException("Store ID must match path");
        }
        ModifierGroup entity = DtoMapper.toEntity(request);
        entity.setStoreId(storeId);
        return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.toResponse(repository.save(entity)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModifierGroupResponse> update(
            @PathVariable Long storeId,
            @PathVariable Long id,
            @Valid @RequestBody ModifierGroupRequest request) {
        ModifierGroup existing = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("ModifierGroup", id));
        if (!storeId.equals(existing.getStoreId())) {
            throw new ResourceNotFoundException("ModifierGroup", id);
        }
        if (request.getStoreId() != null && !request.getStoreId().equals(storeId)) {
            throw new IllegalArgumentException("Store ID must match path");
        }
        ModifierGroup entity = DtoMapper.toEntity(request);
        entity.setId(id);
        entity.setStoreId(storeId);
        return ResponseEntity.ok(DtoMapper.toResponse(repository.save(entity)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long storeId, @PathVariable Long id) {
        ModifierGroup existing = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("ModifierGroup", id));
        if (!storeId.equals(existing.getStoreId())) {
            throw new ResourceNotFoundException("ModifierGroup", id);
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
