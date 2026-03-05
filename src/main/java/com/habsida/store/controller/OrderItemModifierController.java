package com.habsida.store.controller;

import com.habsida.store.entity.OrderItemModifier;
import com.habsida.store.repository.OrderItemModifierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order-item-modifiers")
@RequiredArgsConstructor
public class OrderItemModifierController {

    private final OrderItemModifierRepository repository;

    @GetMapping
    public List<OrderItemModifier> findAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderItemModifier> findById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<OrderItemModifier> create(@RequestBody OrderItemModifier entity) {
        OrderItemModifier saved = repository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderItemModifier> update(@PathVariable Long id, @RequestBody OrderItemModifier entity) {
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
