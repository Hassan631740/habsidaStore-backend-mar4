package com.habsida.store.controller;

import com.habsida.store.entity.StoreBreaks;
import com.habsida.store.repository.StoreBreaksRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/store-breaks")
@RequiredArgsConstructor
public class StoreBreaksController {

    private final StoreBreaksRepository repository;

    @GetMapping
    public List<StoreBreaks> findAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreBreaks> findById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StoreBreaks> create(@RequestBody StoreBreaks entity) {
        StoreBreaks saved = repository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StoreBreaks> update(@PathVariable Long id, @RequestBody StoreBreaks entity) {
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
