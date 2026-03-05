package com.habsida.store.controller;

import com.habsida.store.entity.StoreDeliverySettings;
import com.habsida.store.repository.StoreDeliverySettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/store-delivery-settings")
@RequiredArgsConstructor
public class StoreDeliverySettingsController {

    private final StoreDeliverySettingsRepository repository;

    @GetMapping
    public List<StoreDeliverySettings> findAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreDeliverySettings> findById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StoreDeliverySettings> create(@RequestBody StoreDeliverySettings entity) {
        StoreDeliverySettings saved = repository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StoreDeliverySettings> update(@PathVariable Long id, @RequestBody StoreDeliverySettings entity) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        entity.setId(id);
        return ResponseEntity.ok(repository.save(entity));
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
