package com.habsida.store.controller;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.request.StoreDeliveryAreaRequest;
import com.habsida.store.dto.response.StoreDeliveryAreaResponse;
import com.habsida.store.entity.StoreDeliveryArea;
import com.habsida.store.repository.StoreDeliveryAreaRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/store-delivery-areas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class StoreDeliveryAreaController {

    private final StoreDeliveryAreaRepository repository;

    @GetMapping
    public PageResponse<StoreDeliveryAreaResponse> findAll(Pageable pageable) {
        return PageResponse.of(repository.findAll(pageable).map(DtoMapper::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreDeliveryAreaResponse> findById(@PathVariable Long id) {
        return repository.findById(id)
                .map(DtoMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StoreDeliveryAreaResponse> create(@Valid @RequestBody StoreDeliveryAreaRequest request) {
        StoreDeliveryArea entity = DtoMapper.toEntity(request);
        StoreDeliveryArea saved = repository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StoreDeliveryAreaResponse> update(@PathVariable Long id, @Valid @RequestBody StoreDeliveryAreaRequest request) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        StoreDeliveryArea entity = DtoMapper.toEntity(request);
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
