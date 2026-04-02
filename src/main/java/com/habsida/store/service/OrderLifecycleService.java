package com.habsida.store.service;

import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.response.OrderResponse;
import com.habsida.store.entity.Order;
import com.habsida.store.enums.OrderStatus;
import com.habsida.store.exception.ResourceNotFoundException;
import com.habsida.store.repository.OrderItemRepository;
import com.habsida.store.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

/**
 * Handles order lifecycle transitions: accept, reject, and merchant-driven status updates.
 * Order creation (placement) is in {@link OrderPlacementService}.
 */
@Service
@RequiredArgsConstructor
public class OrderLifecycleService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional
    public OrderResponse merchantAccept(Long orderId, List<Long> merchantStoreIds) {
        Order order = loadOrderForFullStoreOwnership(orderId, merchantStoreIds);
        OrderStatus status = order.getStatus();
        if (status != OrderStatus.NEW && status != OrderStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only NEW or PENDING orders can be accepted");
        }
        order.setStatus(OrderStatus.ACCEPTED);
        order.setAcceptedAt(Instant.now());
        order.setRejectedAt(null);
        order.setRejectReason(null);
        return DtoMapper.toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse merchantReject(Long orderId, List<Long> merchantStoreIds, String rejectReason) {
        Order order = loadOrderForFullStoreOwnership(orderId, merchantStoreIds);
        OrderStatus status = order.getStatus();
        if (status != OrderStatus.NEW && status != OrderStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only NEW or PENDING orders can be rejected");
        }
        order.setStatus(OrderStatus.REJECTED);
        order.setAcceptedAt(null);
        order.setRejectedAt(Instant.now());
        order.setRejectReason(rejectReason);
        return DtoMapper.toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse updateStatusAfterAcceptance(Long orderId, OrderStatus target, List<Long> merchantStoreIds) {
        Order order = loadOrderForFullStoreOwnership(orderId, merchantStoreIds);
        OrderStatus current = order.getStatus();
        if (current == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid order status");
        }
        if (!isAllowedStatusTransition(current, target)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot move from " + current + " to " + target);
        }
        order.setStatus(target);
        if (target == OrderStatus.CANCELED) {
            order.setRejectedAt(Instant.now());
        }
        return DtoMapper.toResponse(orderRepository.save(order));
    }

    private Order loadOrderForFullStoreOwnership(Long orderId, List<Long> merchantStoreIds) {
        if (merchantStoreIds == null || merchantStoreIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Merchant has no assigned stores");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        if (order.getStoreId() != null && merchantStoreIds.contains(order.getStoreId())) {
            return order;
        }
        long totalItems = orderItemRepository.countByOrderId(orderId);
        if (totalItems == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order has no line items");
        }
        long inMerchantStores = orderRepository.countOrderItemsInStores(orderId, merchantStoreIds);
        if (inMerchantStores != totalItems) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Order is not fully fulfilled by your store(s); accept/reject/status apply only to single-store orders");
        }
        return order;
    }

    /**
     * Merchant-driven status transitions after initial accept/reject.
     * NEW/PENDING → CANCELED only. ACCEPTED → IN_PROGRESS → COMPLETED.
     * Terminal states (CANCELED, CANCELLED, COMPLETED, DELIVERED) cannot transition further.
     */
    private static boolean isAllowedStatusTransition(OrderStatus from, OrderStatus to) {
        if (from == OrderStatus.CANCELED || from == OrderStatus.CANCELLED) {
            return false;
        }
        if (to == OrderStatus.CANCELED) {
            return from == OrderStatus.NEW || from == OrderStatus.PENDING
                    || from == OrderStatus.ACCEPTED || from == OrderStatus.IN_PROGRESS;
        }
        if (from == OrderStatus.NEW || from == OrderStatus.PENDING
                || from == OrderStatus.REJECTED || from == OrderStatus.COMPLETED) {
            return false;
        }
        return switch (from) {
            case ACCEPTED -> to == OrderStatus.IN_PROGRESS;
            case IN_PROGRESS -> to == OrderStatus.COMPLETED;
            // Legacy states: map to their nearest canonical equivalent for transition purposes.
            case CONFIRMED -> to == OrderStatus.IN_PROGRESS || to == OrderStatus.COMPLETED;
            case PROCESSING, READY, SHIPPED -> to == OrderStatus.COMPLETED;
            case DELIVERED -> false;
            default -> false;
        };
    }
}