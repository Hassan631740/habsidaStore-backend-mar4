package com.habsida.store.controller;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.CustomerAddressRequest;
import com.habsida.store.dto.response.CustomerAddressResponse;
import com.habsida.store.service.CustomerAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer-addresses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CustomerAddressController {

    private final CustomerAddressService service;

    @GetMapping
    public PageResponse<CustomerAddressResponse> findAll(Pageable pageable) {
        return service.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerAddressResponse> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CustomerAddressResponse> create(@Valid @RequestBody CustomerAddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerAddressResponse> update(@PathVariable Long id, @Valid @RequestBody CustomerAddressRequest request) {
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