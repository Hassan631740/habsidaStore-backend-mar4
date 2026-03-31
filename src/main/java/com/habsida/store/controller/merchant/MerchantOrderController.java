package com.habsida.store.controller.merchant;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.request.MerchantOrderStatusRequest;
import com.habsida.store.dto.response.OrderResponse;
import com.habsida.store.exception.ResourceNotFoundException;
import com.habsida.store.repository.OrderRepository;
import com.habsida.store.repository.UserStoreAccessRepository;
import com.habsida.store.security.AuthUser;
import com.habsida.store.service.OrderWorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Merchant-scoped order access. Lists orders that contain items from the merchant's stores. Requires ROLE_MERCHANT.
 */
@RestController
@RequestMapping("/api/merchant/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
public class MerchantOrderController {

    private final OrderRepository orderRepository;
    private final UserStoreAccessRepository userStoreAccessRepository;
    private final OrderWorkflowService orderWorkflowService;

    @GetMapping
    public PageResponse<OrderResponse> findMyOrders(
            @AuthenticationPrincipal AuthUser authUser,
            Pageable pageable) {
        List<Long> storeIds = userStoreAccessRepository.findByUserId(authUser.getId()).stream()
                .map(usa -> usa.getStoreId())
                .filter(id -> id != null)
                .distinct()
                .toList();
        if (storeIds.isEmpty()) {
            return PageResponse.of(org.springframework.data.domain.Page.empty(pageable));
        }
        return PageResponse.of(
                orderRepository.findByStoreIds(storeIds, pageable).map(DtoMapper::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> findById(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id) {
        List<Long> storeIds = userStoreAccessRepository.findByUserId(authUser.getId()).stream()
                .map(usa -> usa.getStoreId())
                .filter(sid -> sid != null)
                .distinct()
                .toList();
        if (storeIds.isEmpty() || orderRepository.countOrderItemsInStores(id, storeIds) == 0) {
            throw new ResourceNotFoundException("Order", id);
        }
        return ResponseEntity.ok(DtoMapper.toResponse(
                orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order", id))));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<OrderResponse> accept(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id) {
        List<Long> storeIds = merchantStoreIds(authUser.getId());
        return ResponseEntity.ok(orderWorkflowService.merchantAccept(id, storeIds));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<OrderResponse> reject(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id) {
        List<Long> storeIds = merchantStoreIds(authUser.getId());
        return ResponseEntity.ok(orderWorkflowService.merchantReject(id, storeIds));
    }

    /**
     * After accept: CONFIRMED → PROCESSING → READY → SHIPPED → DELIVERED, or CANCELLED (not from PENDING; use reject).
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id,
            @Valid @RequestBody MerchantOrderStatusRequest request) {
        List<Long> storeIds = merchantStoreIds(authUser.getId());
        return ResponseEntity.ok(orderWorkflowService.updateStatusAfterAcceptance(id, request.getStatus(), storeIds));
    }

    private List<Long> merchantStoreIds(Long userId) {
        return userStoreAccessRepository.findByUserId(userId).stream()
                .map(usa -> usa.getStoreId())
                .filter(sid -> sid != null)
                .distinct()
                .toList();
    }
}
