package com.habsida.store.controller;

import com.habsida.store.entity.UserStoreAccess;
import com.habsida.store.repository.UserStoreAccessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-store-access")
@RequiredArgsConstructor
public class UserStoreAccessController {

    private final UserStoreAccessRepository repository;

    @GetMapping
    public List<UserStoreAccess> findAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserStoreAccess> findById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UserStoreAccess> create(@RequestBody UserStoreAccess entity) {
        UserStoreAccess saved = repository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserStoreAccess> update(@PathVariable Long id, @RequestBody UserStoreAccess entity) {
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
