package com.habsida.store.controller;

import com.habsida.store.entity.StoreHours;
import com.habsida.store.repository.StoreHoursRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/store-hours")
@RequiredArgsConstructor
public class StoreHoursController {

    private final StoreHoursRepository repository;

    @GetMapping
    public List<StoreHours> findAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreHours> findById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StoreHours> create(@RequestBody StoreHours entity) {
        StoreHours saved = repository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StoreHours> update(@PathVariable Long id, @RequestBody StoreHours entity) {
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
