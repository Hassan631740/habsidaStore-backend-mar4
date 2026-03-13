package com.habsida.store.controller;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.request.StoreDeliverySettingsRequest;
import com.habsida.store.dto.response.StoreDeliverySettingsResponse;
import com.habsida.store.entity.StoreDeliverySettings;
import com.habsida.store.repository.StoreDeliverySettingsRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/store-delivery-settings")
@RequiredArgsConstructor
public class StoreDeliverySettingsController {

    private final StoreDeliverySettingsRepository repository;

    @GetMapping
    public PageResponse<StoreDeliverySettingsResponse> findAll(Pageable pageable) {
        return PageResponse.of(repository.findAll(pageable).map(DtoMapper::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreDeliverySettingsResponse> findById(@PathVariable Long id) {
        return repository.findById(id)
                .map(DtoMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StoreDeliverySettingsResponse> create(@Valid @RequestBody StoreDeliverySettingsRequest request) {
        StoreDeliverySettings entity = DtoMapper.toEntity(request);
        StoreDeliverySettings saved = repository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StoreDeliverySettingsResponse> update(@PathVariable Long id, @Valid @RequestBody StoreDeliverySettingsRequest request) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        StoreDeliverySettings entity = DtoMapper.toEntity(request);
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
