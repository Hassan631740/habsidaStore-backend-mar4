package com.habsida.store.controller.admin;

import com.habsida.store.dto.request.CreateOrderRequest;
import com.habsida.store.dto.response.OrderResponse;
import com.habsida.store.service.OrderWorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin/system: create order for a store.
 * POST /api/admin/stores/{storeId}/orders
 */
@RestController
@RequestMapping("/api/admin/stores/{storeId}/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminStoreOrderController {

    private final OrderWorkflowService orderWorkflowService;

    @PostMapping
    public ResponseEntity<OrderResponse> create(
            @PathVariable Long storeId,
            @Valid @RequestBody CreateOrderRequest request) {
        OrderResponse created = orderWorkflowService.createOrderForStore(storeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}