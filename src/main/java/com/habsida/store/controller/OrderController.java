package com.habsida.store.controller;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.request.PlaceOrderRequest;
import com.habsida.store.dto.response.OrderResponse;
import com.habsida.store.entity.Customer;
import com.habsida.store.entity.Order;
import com.habsida.store.repository.CustomerRepository;
import com.habsida.store.repository.OrderRepository;
import com.habsida.store.security.AuthUser;
import com.habsida.store.service.OrderPlacementService;
import com.habsida.store.spec.FilterSpecs;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

/**
 * Customer self-service order access. Scoped exclusively to the authenticated customer.
 * Admins use {@code /api/admin/orders} instead.
 */
@RestController
@RequestMapping("/api/me/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class OrderController {

    private static final Map<String, FilterSpecs.FilterMode> ORDER_FILTERS = Map.of(
            "status", FilterSpecs.FilterMode.EQUALS_ORDER_STATUS,
            "customerId", FilterSpecs.FilterMode.EQUALS_LONG
    );

    private final OrderRepository repository;
    private final CustomerRepository customerRepository;
    private final OrderPlacementService orderPlacementService;

    /**
     * Create a single-store order from line items. Status starts as NEW (merchant accept/reject).
     * {@code customerId} is resolved from the JWT-linked customer; any value in the body is ignored.
     */
    @PostMapping("/place")
    public ResponseEntity<OrderResponse> place(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody PlaceOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderPlacementService.placeOrder(request, authUser));
    }

    @GetMapping
    public PageResponse<OrderResponse> findAll(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(required = false) Map<String, String> filter,
            Pageable pageable) {
        Customer customer = customerRepository.findByUserId(authUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No customer profile for this account"));
        Map<String, String> scoped = new HashMap<>();
        if (filter != null) {
            scoped.putAll(filter);
        }
        scoped.put("customerId", String.valueOf(customer.getId()));
        Specification<Order> spec = FilterSpecs.from(scoped, ORDER_FILTERS);
        Page<Order> page = repository.findAll(spec, pageable);
        return PageResponse.of(page.map(DtoMapper::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> findById(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id) {
        Order order = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        Customer customer = customerRepository.findByUserId(authUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No customer profile for this account"));
        if (!customer.getId().equals(order.getCustomerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to access this order");
        }
        return ResponseEntity.ok(DtoMapper.toResponse(order));
    }
}