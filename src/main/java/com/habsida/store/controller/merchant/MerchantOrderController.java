package com.habsida.store.controller.merchant;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.MerchantOrderRejectRequest;
import com.habsida.store.dto.request.MerchantOrderStatusRequest;
import com.habsida.store.dto.response.OrderResponse;
import com.habsida.store.security.AuthUser;
import com.habsida.store.service.OrderAdminService;
import com.habsida.store.service.OrderWorkflowService;
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
public class MerchantOrderController {

    private final OrderAdminService orderQueryService;
    private final OrderWorkflowService orderWorkflowService;

    /**
     * List orders for the merchant's stores. Optional filter: ?status=NEW (or other OrderStatus).
     */
    @GetMapping
    public PageResponse<OrderResponse> findMyOrders(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        return orderQueryService.getMerchantOrders(authUser.getId(), status, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> findById(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id) {
        return ResponseEntity.ok(orderQueryService.getMerchantOrder(authUser.getId(), id));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<OrderResponse> accept(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id) {
        return ResponseEntity.ok(orderWorkflowService.merchantAccept(id, authUser.getId()));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<OrderResponse> reject(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id,
            @RequestBody(required = false) MerchantOrderRejectRequest body) {
        String reason = body != null ? body.getRejectReason() : null;
        return ResponseEntity.ok(orderWorkflowService.merchantReject(id, authUser.getId(), reason));
    }

    /**
     * Lifecycle:
     * NEW → ACCEPTED → IN_PROGRESS → COMPLETED
     * NEW → REJECTED / CANCELED
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id,
            @Valid @RequestBody MerchantOrderStatusRequest request) {
        return ResponseEntity.ok(orderWorkflowService.updateStatusAfterAcceptance(id, request.getStatus(), authUser.getId()));
    }
}