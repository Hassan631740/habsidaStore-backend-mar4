package com.habsida.store.service;

import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.request.CreateOrderRequest;
import com.habsida.store.dto.request.PlaceOrderRequest;
import com.habsida.store.dto.response.OrderResponse;
import com.habsida.store.entity.Customer;
import com.habsida.store.entity.ModifierOption;
import com.habsida.store.entity.Order;
import com.habsida.store.entity.OrderItem;
import com.habsida.store.entity.OrderItemModifier;
import com.habsida.store.entity.Product;
import com.habsida.store.enums.CustomerStatus;
import com.habsida.store.enums.OrderStatus;
import com.habsida.store.exception.ResourceNotFoundException;
import com.habsida.store.repository.CustomerRepository;
import com.habsida.store.repository.ModifierOptionRepository;
import com.habsida.store.repository.OrderItemModifierRepository;
import com.habsida.store.repository.OrderItemRepository;
import com.habsida.store.repository.OrderRepository;
import com.habsida.store.repository.ProductRepository;
import com.habsida.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderWorkflowService {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemModifierRepository orderItemModifierRepository;
    private final ModifierOptionRepository modifierOptionRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", request.getCustomerId()));
        if (!CustomerStatus.ACTIVE.name().equals(customer.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer account is suspended");
        }

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();
        for (PlaceOrderRequest.OrderLineRequest line : request.getLines()) {
            Product p = productRepository.findById(line.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", line.getProductId()));
            if (p.getStoreId() == null || !p.getStoreId().equals(request.getStoreId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Product " + line.getProductId() + " does not belong to this store");
            }
            if (Boolean.FALSE.equals(p.getAvailableForOrder())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Product " + line.getProductId() + " is not available for order");
            }
            BigDecimal unit = p.getPrice() != null ? p.getPrice() : BigDecimal.ZERO;
            total = total.add(unit.multiply(BigDecimal.valueOf(line.getQuantity())));
            items.add(OrderItem.builder()
                    .productId(p.getId())
                    .productNameSnapshot(p.getName())
                    .unitPriceSnapshot(unit)
                    .quantity(line.getQuantity())
                    .price(unit)
                    .build());
        }

        Order order = Order.builder()
                .storeId(request.getStoreId())
                .customerId(request.getCustomerId())
                .status(OrderStatus.PENDING.name())
                .orderType(request.getOrderType() != null ? request.getOrderType().name() : null)
                .totalAmount(total)
                .build();
        Order saved = orderRepository.save(order);
        for (OrderItem oi : items) {
            oi.setOrderId(saved.getId());
        }
        orderItemRepository.saveAll(items);
        return DtoMapper.toResponse(saved);
    }

    @Transactional
    public OrderResponse createOrderForStore(Long storeId, CreateOrderRequest request) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", storeId);
        }
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", request.getCustomerId()));
        if (!CustomerStatus.ACTIVE.name().equals(customer.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer account is suspended");
        }

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();
        List<List<OrderItemModifier>> modifiersPerLine = new ArrayList<>();
        for (CreateOrderRequest.CreateOrderLineRequest line : request.getLines()) {
            Product p = productRepository.findById(line.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", line.getProductId()));
            if (p.getStoreId() == null || !p.getStoreId().equals(storeId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Product " + line.getProductId() + " does not belong to this store");
            }
            if (Boolean.FALSE.equals(p.getAvailableForOrder())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Product " + line.getProductId() + " is not available for order");
            }
            BigDecimal unit = p.getPrice() != null ? p.getPrice() : BigDecimal.ZERO;
            BigDecimal lineTotal = unit.multiply(BigDecimal.valueOf(line.getQuantity()));
            List<OrderItemModifier> modifiers = new ArrayList<>();
            if (line.getModifierOptionIds() != null) {
                for (Long optionId : line.getModifierOptionIds()) {
                    ModifierOption opt = modifierOptionRepository.findById(optionId)
                            .orElseThrow(() -> new ResourceNotFoundException("ModifierOption", optionId));
                    BigDecimal adj = opt.getPriceAdjustment() != null ? opt.getPriceAdjustment() : BigDecimal.ZERO;
                    lineTotal = lineTotal.add(adj.multiply(BigDecimal.valueOf(line.getQuantity())));
                    modifiers.add(OrderItemModifier.builder()
                            .modifierOptionId(opt.getId())
                            .optionNameSnapshot(opt.getName())
                            .price(opt.getPriceAdjustment())
                            .build());
                }
            }
            total = total.add(lineTotal);
            items.add(OrderItem.builder()
                    .productId(p.getId())
                    .productNameSnapshot(p.getName())
                    .unitPriceSnapshot(unit)
                    .quantity(line.getQuantity())
                    .price(unit)
                    .build());
            modifiersPerLine.add(modifiers);
        }

        Order order = Order.builder()
                .storeId(storeId)
                .customerId(request.getCustomerId())
                .status(OrderStatus.NEW.name())
                .orderType(request.getOrderType() != null ? request.getOrderType().name() : null)
                .totalAmount(total)
                .notes(request.getNotes())
                .build();
        Order saved = orderRepository.save(order);
        for (int i = 0; i < items.size(); i++) {
            OrderItem oi = items.get(i);
            oi.setOrderId(saved.getId());
            OrderItem savedItem = orderItemRepository.save(oi);
            for (OrderItemModifier m : modifiersPerLine.get(i)) {
                m.setOrderItemId(savedItem.getId());
                orderItemModifierRepository.save(m);
            }
        }
        return DtoMapper.toResponse(orderRepository.findById(saved.getId()).orElseThrow());
    }

    @Transactional
    public OrderResponse merchantAccept(Long orderId, List<Long> merchantStoreIds) {
        Order order = loadOrderForFullStoreOwnership(orderId, merchantStoreIds);
        String status = order.getStatus();
        if (!OrderStatus.NEW.name().equals(status) && !OrderStatus.PENDING.name().equals(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only NEW or PENDING orders can be accepted");
        }
        order.setStatus(OrderStatus.CONFIRMED.name());
        order.setAcceptedAt(Instant.now());
        order.setRejectReason(null);
        return DtoMapper.toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse merchantReject(Long orderId, List<Long> merchantStoreIds, String rejectReason) {
        Order order = loadOrderForFullStoreOwnership(orderId, merchantStoreIds);
        String status = order.getStatus();
        if (!OrderStatus.NEW.name().equals(status) && !OrderStatus.PENDING.name().equals(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only NEW or PENDING orders can be rejected");
        }
        order.setStatus(OrderStatus.REJECTED.name());
        order.setRejectReason(rejectReason);
        return DtoMapper.toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse updateStatusAfterAcceptance(Long orderId, OrderStatus target, List<Long> merchantStoreIds) {
        Order order = loadOrderForFullStoreOwnership(orderId, merchantStoreIds);
        OrderStatus current;
        try {
            current = OrderStatus.valueOf(order.getStatus());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid order status");
        }
        if (!isAllowedStatusTransition(current, target)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot move from " + current + " to " + target);
        }
        order.setStatus(target.name());
        return DtoMapper.toResponse(orderRepository.save(order));
    }

    private Order loadOrderForFullStoreOwnership(Long orderId, List<Long> merchantStoreIds) {
        if (merchantStoreIds == null || merchantStoreIds.isEmpty()) {
            throw new ResourceNotFoundException("Order", orderId);
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
     * After placement: NEW/PENDING → accept/reject only. Then CONFIRMED → … → DELIVERED, or CANCELLED from most states.
     */
    private static boolean isAllowedStatusTransition(OrderStatus from, OrderStatus to) {
        if (from == OrderStatus.NEW || from == OrderStatus.PENDING || from == OrderStatus.REJECTED) {
            return false;
        }
        if (from == OrderStatus.DELIVERED) {
            return false;
        }
        if (to == OrderStatus.CANCELLED) {
            return from != OrderStatus.DELIVERED && from != OrderStatus.REJECTED;
        }
        return switch (from) {
            case CONFIRMED -> to == OrderStatus.PROCESSING;
            case PROCESSING -> to == OrderStatus.READY;
            case READY -> to == OrderStatus.SHIPPED;
            case SHIPPED -> to == OrderStatus.DELIVERED;
            default -> false;
        };
    }
}
