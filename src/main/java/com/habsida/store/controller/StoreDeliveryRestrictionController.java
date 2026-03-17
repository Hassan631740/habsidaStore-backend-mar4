package com.habsida.store.controller;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.request.StoreDeliveryRestrictionRequest;
import com.habsida.store.dto.response.StoreDeliveryRestrictionResponse;
import com.habsida.store.entity.StoreDeliveryRestriction;
import com.habsida.store.repository.StoreDeliveryRestrictionRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/store-delivery-restrictions")
@RequiredArgsConstructor
public class StoreDeliveryRestrictionController {

    private final StoreDeliveryRestrictionRepository repository;

    @GetMapping
    public PageResponse<StoreDeliveryRestrictionResponse> findAll(Pageable pageable) {
        return PageResponse.of(repository.findAll(pageable).map(DtoMapper::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreDeliveryRestrictionResponse> findById(@PathVariable Long id) {
        return repository.findById(id)
                .map(DtoMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StoreDeliveryRestrictionResponse> create(@Valid @RequestBody StoreDeliveryRestrictionRequest request) {
        StoreDeliveryRestriction entity = DtoMapper.toEntity(request);
        StoreDeliveryRestriction saved = repository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StoreDeliveryRestrictionResponse> update(@PathVariable Long id, @Valid @RequestBody StoreDeliveryRestrictionRequest request) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        StoreDeliveryRestriction entity = DtoMapper.toEntity(request);
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
