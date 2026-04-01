package com.habsida.store.controller;

import com.habsida.store.dto.request.PlaceOrderRequest;
import com.habsida.store.dto.response.OrderResponse;
import com.habsida.store.service.OrderWorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class OrderController {

    private final OrderWorkflowService orderWorkflowService;

    /**
     * Place a new order. Authenticated customers only.
     * Status starts as PENDING (merchant must accept/reject via merchant workflow).
     */
    @PostMapping("/place")
    public ResponseEntity<OrderResponse> place(@Valid @RequestBody PlaceOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderWorkflowService.placeOrder(request));
    }
}
