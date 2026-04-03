package com.habsida.store.controller;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.StoreDeliveryRestrictionRequest;
import com.habsida.store.dto.response.StoreDeliveryRestrictionResponse;
import com.habsida.store.service.StoreDeliveryRestrictionService;
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

    private final StoreDeliveryRestrictionService service;

    @GetMapping
    public PageResponse<StoreDeliveryRestrictionResponse> findAll(Pageable pageable) {
        return service.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreDeliveryRestrictionResponse> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StoreDeliveryRestrictionResponse> create(@Valid @RequestBody StoreDeliveryRestrictionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StoreDeliveryRestrictionResponse> update(@PathVariable Long id, @Valid @RequestBody StoreDeliveryRestrictionRequest request) {
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