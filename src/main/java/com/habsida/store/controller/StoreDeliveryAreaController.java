package com.habsida.store.controller;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.StoreDeliveryAreaRequest;
import com.habsida.store.dto.response.StoreDeliveryAreaResponse;
import com.habsida.store.service.StoreDeliveryAreaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/store-delivery-areas")
@RequiredArgsConstructor
public class StoreDeliveryAreaController {

    private final StoreDeliveryAreaService service;

    @GetMapping
    public PageResponse<StoreDeliveryAreaResponse> findAll(Pageable pageable) {
        return service.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreDeliveryAreaResponse> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StoreDeliveryAreaResponse> create(@Valid @RequestBody StoreDeliveryAreaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StoreDeliveryAreaResponse> update(@PathVariable Long id, @Valid @RequestBody StoreDeliveryAreaRequest request) {
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