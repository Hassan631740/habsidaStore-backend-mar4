package com.habsida.store.service;

import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.response.OrderPaymentResponse;
import com.habsida.store.entity.Order;
import com.habsida.store.entity.OrderPayment;
import com.habsida.store.enums.OrderStatus;
import com.habsida.store.enums.PaymentStatus;
import com.habsida.store.exception.ResourceNotFoundException;
import com.habsida.store.repository.OrderPaymentRepository;
import com.habsida.store.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MerchantPaymentService {

    private final OrderPaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final MerchantStoreAccessService merchantStoreAccessService;

    @Transactional(readOnly = true)
    public OrderPaymentResponse getPaymentForOrder(Long orderId, Long merchantUserId) {
        assertMerchantOwnsOrder(orderId, merchantUserId);
        return paymentRepository.findByOrderId(orderId)
                .map(DtoMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("OrderPayment for order", orderId));
    }

    /**
     * Status transition rules:
     * PENDING → COMPLETED  (mark paid)
     * PENDING → CANCELLED
     * COMPLETED → REFUNDED (only when the order itself is REJECTED or CANCELED)
     */
    @Transactional
    public OrderPaymentResponse updatePaymentStatus(Long orderId, PaymentStatus newStatus, Long merchantUserId) {
        assertMerchantOwnsOrder(orderId, merchantUserId);

        OrderPayment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("OrderPayment for order", orderId));

        PaymentStatus current = parseStatus(payment.getStatus());
        validateTransition(current, newStatus, orderId);

        payment.setStatus(newStatus.name());
        return DtoMapper.toResponse(paymentRepository.save(payment));
    }

    // ---- helpers ----

    private void assertMerchantOwnsOrder(Long orderId, Long merchantUserId) {
        List<Long> storeIds = merchantStoreAccessService.getStoreIds(merchantUserId);
        if (storeIds.isEmpty() || !orderRepository.existsByIdAndStoreIds(orderId, storeIds)) {
            throw new ResourceNotFoundException("Order", orderId);
        }
    }

    private PaymentStatus parseStatus(String raw) {
        if (raw == null) return null;
        try {
            return PaymentStatus.valueOf(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void validateTransition(PaymentStatus current, PaymentStatus next, Long orderId) {
        if (current == PaymentStatus.PENDING && next == PaymentStatus.COMPLETED) return;
        if (current == PaymentStatus.PENDING && next == PaymentStatus.CANCELLED) return;
        if (current == PaymentStatus.COMPLETED && next == PaymentStatus.REFUNDED) {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
            OrderStatus orderStatus = order.getStatus();
            if (orderStatus == OrderStatus.REJECTED || orderStatus == OrderStatus.CANCELED
                    || orderStatus == OrderStatus.CANCELLED) {
                return;
            }
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Refund is only allowed when the order is REJECTED or CANCELED");
        }
        throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                "Invalid payment status transition: " + current + " → " + next);
    }
}