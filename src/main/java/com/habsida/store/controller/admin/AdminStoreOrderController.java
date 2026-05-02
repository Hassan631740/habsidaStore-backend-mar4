package com.habsida.store.controller.admin;

import com.habsida.store.dto.request.CreateOrderRequest;
import com.habsida.store.dto.response.OrderResponse;
import com.habsida.store.service.OrderPlacementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/stores/{storeId}/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Orders", description = "Admin: full order management across all stores")
public class AdminStoreOrderController {

    private final OrderPlacementService orderPlacementService;

    @Operation(summary = "Create an order for a specific store (admin override)")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "404", description = "Store not found")
    })
    @PostMapping
    public ResponseEntity<OrderResponse> create(
            @PathVariable Long storeId,
            @Valid @RequestBody CreateOrderRequest request) {
        OrderResponse created = orderPlacementService.createOrderForStore(storeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}