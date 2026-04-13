package com.habsida.store.service;

import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.OrderRequest;
import com.habsida.store.dto.response.OrderResponse;
import com.habsida.store.entity.Order;
import com.habsida.store.entity.OrderItem;
import com.habsida.store.enums.OrderStatus;
import com.habsida.store.exception.ResourceNotFoundException;
import com.habsida.store.repository.OrderItemRepository;
import com.habsida.store.repository.OrderRepository;
import com.habsida.store.spec.FilterSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderAdminService {

    private static final Map<String, FilterSpecs.FilterMode> ORDER_FILTERS = Map.of(
            "status", FilterSpecs.FilterMode.EQUALS,
            "customerId", FilterSpecs.FilterMode.EQUALS_LONG
    );

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MerchantStoreAccessService merchantStoreAccessService;

    // ─── Merchant reads ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getMerchantOrders(Long userId, String status, Pageable pageable) {
        if (status != null && !status.isEmpty()) {
            try {
                OrderStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid order status: " + status);
            }
        }
        List<Long> storeIds = merchantStoreAccessService.getStoreIds(userId);
        if (storeIds.isEmpty()) {
            return PageResponse.of(Page.empty(pageable));
        }
        return PageResponse.of(
                orderRepository.findByStoreIdsAndStatus(storeIds, status, pageable).map(DtoMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public OrderResponse getMerchantOrder(Long userId, Long orderId) {
        List<Long> storeIds = merchantStoreAccessService.getStoreIds(userId);
        if (storeIds.isEmpty() || !orderRepository.existsByIdAndStoreIds(orderId, storeIds)) {
            throw new ResourceNotFoundException("Order", orderId);
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        return DtoMapper.toResponse(order, items);
    }

    // ─── Admin reads ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getAdminOrders(Map<String, String> filter, Pageable pageable) {
        Specification<Order> spec = FilterSpecs.from(filter, ORDER_FILTERS);
        Page<Order> page = spec == null ? orderRepository.findAll(pageable) : orderRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(DtoMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public OrderResponse getAdminOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        return DtoMapper.toResponse(order, items);
    }

    // ─── Admin writes ─────────────────────────────────────────────────────────

    /**
     * Admin-created orders always start at NEW regardless of the status field in the request,
     * to enforce the workflow state machine from the beginning.
     */
    @Transactional
    public OrderResponse createAdminOrder(OrderRequest request) {
        Order entity = DtoMapper.toEntity(request);
        entity.setStatus(OrderStatus.NEW);
        Order saved = orderRepository.save(entity);
        List<OrderItem> items = orderItemRepository.findByOrderId(saved.getId());
        return DtoMapper.toResponse(saved, items);
    }

    @Transactional
    public OrderResponse updateAdminOrder(Long id, OrderRequest request) {
        Order existing = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        existing.setStoreId(request.getStoreId());
        existing.setCustomerId(request.getCustomerId());
        existing.setStatus(request.getStatus());
        existing.setOrderType(request.getOrderType());
        existing.setTotalAmount(request.getTotalAmount());
        existing.setNotes(request.getNotes());
        Order saved = orderRepository.save(existing);
        List<OrderItem> items = orderItemRepository.findByOrderId(saved.getId());
        return DtoMapper.toResponse(saved, items);
    }

    @Transactional
    public void deleteAdminOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Order", id);
        }
        orderRepository.deleteById(id);
    }
}