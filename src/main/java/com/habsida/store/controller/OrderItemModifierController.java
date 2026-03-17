package com.habsida.store.controller;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.request.OrderItemModifierRequest;
import com.habsida.store.dto.response.OrderItemModifierResponse;
import com.habsida.store.entity.OrderItemModifier;
import com.habsida.store.repository.OrderItemModifierRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order-item-modifiers")
@RequiredArgsConstructor
public class OrderItemModifierController {

    private final OrderItemModifierRepository repository;

    @GetMapping
    public PageResponse<OrderItemModifierResponse> findAll(Pageable pageable) {
        return PageResponse.of(repository.findAll(pageable).map(DtoMapper::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderItemModifierResponse> findById(@PathVariable Long id) {
        return repository.findById(id)
                .map(DtoMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<OrderItemModifierResponse> create(@Valid @RequestBody OrderItemModifierRequest request) {
        OrderItemModifier entity = DtoMapper.toEntity(request);
        OrderItemModifier saved = repository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderItemModifierResponse> update(@PathVariable Long id, @Valid @RequestBody OrderItemModifierRequest request) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        OrderItemModifier entity = DtoMapper.toEntity(request);
        entity.setId(id);
        return ResponseEntity.ok(DtoMapper.toResponse(repository.save(entity)));
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
