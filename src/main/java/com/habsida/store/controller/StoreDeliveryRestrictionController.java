package com.habsida.store.controller;

import com.habsida.store.entity.StoreDeliveryRestriction;
import com.habsida.store.repository.StoreDeliveryRestrictionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/store-delivery-restrictions")
@RequiredArgsConstructor
public class StoreDeliveryRestrictionController {

    private final StoreDeliveryRestrictionRepository repository;

    @GetMapping
    public List<StoreDeliveryRestriction> findAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreDeliveryRestriction> findById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StoreDeliveryRestriction> create(@RequestBody StoreDeliveryRestriction entity) {
        StoreDeliveryRestriction saved = repository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StoreDeliveryRestriction> update(@PathVariable Long id, @RequestBody StoreDeliveryRestriction entity) {
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
