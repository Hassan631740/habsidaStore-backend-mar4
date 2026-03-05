package com.habsida.store.controller;

import com.habsida.store.entity.OrderAddress;
import com.habsida.store.repository.OrderAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order-addresses")
@RequiredArgsConstructor
public class OrderAddressController {

    private final OrderAddressRepository repository;

    @GetMapping
    public List<OrderAddress> findAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderAddress> findById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<OrderAddress> create(@RequestBody OrderAddress entity) {
        OrderAddress saved = repository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderAddress> update(@PathVariable Long id, @RequestBody OrderAddress entity) {
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
