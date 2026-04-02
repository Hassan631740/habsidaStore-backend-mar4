package com.habsida.store.controller.admin;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.request.OrderRequest;
import com.habsida.store.dto.response.OrderResponse;
import com.habsida.store.entity.Order;
import com.habsida.store.enums.OrderStatus;
import com.habsida.store.exception.ResourceNotFoundException;
import com.habsida.store.repository.OrderRepository;
import com.habsida.store.spec.FilterSpecs;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

/**
 * Admin-only order management. Requires ROLE_ADMIN.
 */
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private static final Map<String, FilterSpecs.FilterMode> ORDER_FILTERS = Map.of(
            "status", FilterSpecs.FilterMode.EQUALS_ORDER_STATUS,
            "customerId", FilterSpecs.FilterMode.EQUALS_LONG
    );

    private final OrderRepository repository;

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
        Order order = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        return ResponseEntity.ok(DtoMapper.toResponse(order));
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request) {
        Order entity = DtoMapper.toEntity(request);
        if (request.getStatus() == OrderStatus.ACCEPTED) {
            entity.setAcceptedAt(Instant.now());
        } else if (request.getStatus() == OrderStatus.REJECTED) {
            entity.setRejectedAt(Instant.now());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.toResponse(repository.save(entity)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> update(@PathVariable Long id, @Valid @RequestBody OrderRequest request) {
        Order existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        // Mutate the loaded entity to preserve audit timestamps and DB-generated fields
        existing.setStoreId(request.getStoreId());
        existing.setCustomerId(request.getCustomerId());
        existing.setOrderType(request.getOrderType() != null ? request.getOrderType().name() : null);
        existing.setTotalAmount(request.getTotalAmount());
        existing.setNotes(request.getNotes());
        OrderStatus prev = existing.getStatus();
        existing.setStatus(request.getStatus());
        if (request.getStatus() == OrderStatus.ACCEPTED && prev != OrderStatus.ACCEPTED) {
            existing.setAcceptedAt(Instant.now());
            existing.setRejectedAt(null);
            existing.setRejectReason(null);
        } else if (request.getStatus() == OrderStatus.REJECTED && prev != OrderStatus.REJECTED) {
            existing.setRejectedAt(Instant.now());
            existing.setAcceptedAt(null);
        }
        return ResponseEntity.ok(DtoMapper.toResponse(repository.save(existing)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Order", id);
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
