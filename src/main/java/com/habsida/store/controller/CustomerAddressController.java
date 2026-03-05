package com.habsida.store.controller;

import com.habsida.store.entity.CustomerAddress;
import com.habsida.store.repository.CustomerAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer-addresses")
@RequiredArgsConstructor
public class CustomerAddressController {

    private final CustomerAddressRepository repository;

    @GetMapping
    public List<CustomerAddress> findAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerAddress> findById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CustomerAddress> create(@RequestBody CustomerAddress entity) {
        CustomerAddress saved = repository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerAddress> update(@PathVariable Long id, @RequestBody CustomerAddress entity) {
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
