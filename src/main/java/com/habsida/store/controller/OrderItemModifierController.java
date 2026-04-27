package com.habsida.store.controller;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.OrderItemModifierRequest;
import com.habsida.store.dto.response.OrderItemModifierResponse;
import com.habsida.store.service.OrderItemModifierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order-item-modifiers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class OrderItemModifierController {

    private final OrderItemModifierService service;

    @GetMapping
    public PageResponse<OrderItemModifierResponse> findAll(Pageable pageable) {
        return service.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderItemModifierResponse> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<OrderItemModifierResponse> create(@Valid @RequestBody OrderItemModifierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderItemModifierResponse> update(@PathVariable Long id, @Valid @RequestBody OrderItemModifierRequest request) {
        return service.update(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}