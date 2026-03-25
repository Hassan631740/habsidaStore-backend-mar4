package com.habsida.store.controller;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.request.FulfillmentRequest;
import com.habsida.store.dto.response.FulfillmentResponse;
import com.habsida.store.entity.Fulfillment;
import com.habsida.store.repository.FulfillmentRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fulfillment")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class FulfillmentController {

    private final FulfillmentRepository repository;

    @GetMapping
    public PageResponse<FulfillmentResponse> findAll(Pageable pageable) {
        return PageResponse.of(repository.findAll(pageable).map(DtoMapper::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FulfillmentResponse> findById(@PathVariable Long id) {
        return repository.findById(id)
                .map(DtoMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<FulfillmentResponse> create(@Valid @RequestBody FulfillmentRequest request) {
        Fulfillment entity = DtoMapper.toEntity(request);
        Fulfillment saved = repository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FulfillmentResponse> update(@PathVariable Long id, @Valid @RequestBody FulfillmentRequest request) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        Fulfillment entity = DtoMapper.toEntity(request);
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
