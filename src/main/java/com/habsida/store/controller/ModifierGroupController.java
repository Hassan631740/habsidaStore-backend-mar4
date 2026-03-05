package com.habsida.store.controller;

import com.habsida.store.entity.ModifierGroup;
import com.habsida.store.repository.ModifierGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modifier-groups")
@RequiredArgsConstructor
public class ModifierGroupController {

    private final ModifierGroupRepository repository;

    @GetMapping
    public List<ModifierGroup> findAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModifierGroup> findById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ModifierGroup> create(@RequestBody ModifierGroup entity) {
        ModifierGroup saved = repository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModifierGroup> update(@PathVariable Long id, @RequestBody ModifierGroup entity) {
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
