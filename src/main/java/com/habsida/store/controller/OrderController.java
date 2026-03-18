package com.habsida.store.controller;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.request.OrderRequest;
import com.habsida.store.dto.request.PlaceOrderRequest;
import com.habsida.store.dto.response.OrderResponse;
import com.habsida.store.entity.Order;
import com.habsida.store.repository.OrderRepository;
import com.habsida.store.service.OrderWorkflowService;
import com.habsida.store.spec.FilterSpecs;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private static final Map<String, FilterSpecs.FilterMode> ORDER_FILTERS = Map.of(
            "status", FilterSpecs.FilterMode.EQUALS,
            "customerId", FilterSpecs.FilterMode.EQUALS_LONG
    );

    private final OrderRepository repository;
    private final OrderWorkflowService orderWorkflowService;

    /**
     * Create a single-store order from line items. Status starts as NEW (merchant accept/reject).
     */
    @PostMapping("/place")
    public ResponseEntity<OrderResponse> place(@Valid @RequestBody PlaceOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderWorkflowService.placeOrder(request));
    }

    @GetMapping
    public PageResponse<OrderResponse> findAll(
            @RequestParam(required = false) Map<String, String> filter,
            Pageable pageable) {
        Specification<Order> spec = FilterSpecs.from(filter, ORDER_FILTERS);
        Page<Order> page = spec == null ? repository.findAll(pageable) : repository.findAll(spec, pageable);
        return PageResponse.of(page.map(DtoMapper::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> findById(@PathVariable Long id) {
        return repository.findById(id)
                .map(DtoMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request) {
        Order entity = DtoMapper.toEntity(request);
        Order saved = repository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> update(@PathVariable Long id, @Valid @RequestBody OrderRequest request) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        Order entity = DtoMapper.toEntity(request);
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
