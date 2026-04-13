package com.habsida.store.controller;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.StoreDeliverySettingsRequest;
import com.habsida.store.dto.response.StoreDeliverySettingsResponse;
import com.habsida.store.service.StoreDeliverySettingsService;
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

    private final StoreDeliverySettingsService service;

    @GetMapping
    public PageResponse<StoreDeliverySettingsResponse> findAll(Pageable pageable) {
        return service.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreDeliverySettingsResponse> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StoreDeliverySettingsResponse> create(@Valid @RequestBody StoreDeliverySettingsRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StoreDeliverySettingsResponse> update(@PathVariable Long id, @Valid @RequestBody StoreDeliverySettingsRequest request) {
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