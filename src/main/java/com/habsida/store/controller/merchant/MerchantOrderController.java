package com.habsida.store.controller.merchant;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.MerchantOrderRejectRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.habsida.store.dto.request.MerchantOrderStatusRequest;
import com.habsida.store.dto.request.PaymentStatusUpdateRequest;
import com.habsida.store.dto.response.OrderPaymentResponse;
import com.habsida.store.dto.response.OrderResponse;
import com.habsida.store.security.AuthUser;
import com.habsida.store.service.MerchantPaymentService;
import com.habsida.store.service.MerchantStoreAccessService;
import com.habsida.store.service.OrderAdminService;
import com.habsida.store.service.OrderLifecycleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Merchant-scoped order access. Lists orders that contain items from the merchant's stores. Requires ROLE_MERCHANT.
 */
@RestController
@RequestMapping("/api/merchant/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
@Tag(name = "Merchant Orders", description = "Merchant: list, accept, reject, and advance order lifecycle")
public class MerchantOrderController {

    private final OrderAdminService orderQueryService;
    private final OrderLifecycleService orderLifecycleService;
    private final MerchantStoreAccessService merchantStoreAccessService;
    private final MerchantPaymentService merchantPaymentService;

    /**
     * List orders for the merchant's stores. Optional filter: ?status=NEW (or other OrderStatus).
     */
    @Operation(summary = "List orders for the merchant's stores (filterable by status)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", description = "Unauthorised"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping
    public PageResponse<OrderResponse> findMyOrders(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        return orderQueryService.getMerchantOrders(authUser.getId(), status, pageable);
    }

    @Operation(summary = "Get a single order from the merchant's store")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "403", description = "Order not from merchant's store"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> findById(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id) {
        return ResponseEntity.ok(orderQueryService.getMerchantOrder(authUser.getId(), id));
    }

    @Operation(summary = "Accept an order (NEW or PENDING → ACCEPTED)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order accepted"),
        @ApiResponse(responseCode = "400", description = "Order is not in NEW or PENDING state"),
        @ApiResponse(responseCode = "403", description = "Order belongs to a different store")
    })
    @PostMapping("/{id}/accept")
    public ResponseEntity<OrderResponse> accept(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id) {
        return ResponseEntity.ok(
                orderLifecycleService.merchantAccept(id, merchantStoreAccessService.getStoreIds(authUser.getId())));
    }

    @Operation(summary = "Reject an order (NEW or PENDING → REJECTED)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order rejected"),
        @ApiResponse(responseCode = "400", description = "Order cannot be rejected in its current state"),
        @ApiResponse(responseCode = "403", description = "Order belongs to a different store")
    })
    @PostMapping("/{id}/reject")
    public ResponseEntity<OrderResponse> reject(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id,
            @RequestBody(required = false) MerchantOrderRejectRequest body) {
        String reason = body != null ? body.getRejectReason() : null;
        return ResponseEntity.ok(
                orderLifecycleService.merchantReject(id, merchantStoreAccessService.getStoreIds(authUser.getId()), reason));
    }

    /**
     * Lifecycle:
     * NEW → ACCEPTED → IN_PROGRESS → COMPLETED
     * NEW → REJECTED / CANCELED
     */
    @Operation(summary = "Advance order status (ACCEPTED→IN_PROGRESS→COMPLETED; any→CANCELED)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status updated"),
        @ApiResponse(responseCode = "400", description = "Transition not allowed from current state"),
        @ApiResponse(responseCode = "403", description = "Order belongs to a different store")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id,
            @Valid @RequestBody MerchantOrderStatusRequest request) {
        return ResponseEntity.ok(
                orderLifecycleService.updateStatusAfterAcceptance(
                        id, request.getStatus(), merchantStoreAccessService.getStoreIds(authUser.getId())));
    }

    @Operation(summary = "Get payment record for an order")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping("/{id}/payment")
    public ResponseEntity<OrderPaymentResponse> getPayment(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id) {
        return ResponseEntity.ok(merchantPaymentService.getPaymentForOrder(id, authUser.getId()));
    }

    @Operation(summary = "Update payment status for an order")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Payment status updated"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "403", description = "Order belongs to a different store")
    })
    @PatchMapping("/{id}/payment")
    public ResponseEntity<OrderPaymentResponse> updatePaymentStatus(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id,
            @Valid @RequestBody PaymentStatusUpdateRequest request) {
        return ResponseEntity.ok(merchantPaymentService.updatePaymentStatus(id, request.getStatus(), authUser.getId()));
    }
}