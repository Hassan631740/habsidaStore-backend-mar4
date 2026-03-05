package com.habsida.store.controller;

import com.habsida.store.entity.StoreDeliveryArea;
import com.habsida.store.repository.StoreDeliveryAreaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/store-delivery-areas")
@RequiredArgsConstructor
public class StoreDeliveryAreaController {

    private final StoreDeliveryAreaRepository repository;

    @GetMapping
    public List<StoreDeliveryArea> findAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreDeliveryArea> findById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StoreDeliveryArea> create(@RequestBody StoreDeliveryArea entity) {
        StoreDeliveryArea saved = repository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StoreDeliveryArea> update(@PathVariable Long id, @RequestBody StoreDeliveryArea entity) {
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
