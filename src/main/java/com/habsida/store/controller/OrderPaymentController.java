package com.habsida.store.controller;

import com.habsida.store.entity.OrderPayment;
import com.habsida.store.repository.OrderPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order-payments")
@RequiredArgsConstructor
public class OrderPaymentController {

    private final OrderPaymentRepository repository;

    @GetMapping
    public List<OrderPayment> findAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderPayment> findById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<OrderPayment> create(@RequestBody OrderPayment entity) {
        OrderPayment saved = repository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderPayment> update(@PathVariable Long id, @RequestBody OrderPayment entity) {
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
