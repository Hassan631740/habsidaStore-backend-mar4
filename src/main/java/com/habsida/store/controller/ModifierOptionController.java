package com.habsida.store.controller;

import com.habsida.store.entity.ModifierOption;
import com.habsida.store.repository.ModifierOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modifier-options")
@RequiredArgsConstructor
public class ModifierOptionController {

    private final ModifierOptionRepository repository;

    @GetMapping
    public List<ModifierOption> findAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModifierOption> findById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ModifierOption> create(@RequestBody ModifierOption entity) {
        ModifierOption saved = repository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModifierOption> update(@PathVariable Long id, @RequestBody ModifierOption entity) {
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
