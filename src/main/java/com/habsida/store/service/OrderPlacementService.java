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
import com.habsida.store.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles order placement: customer self-service placement and admin/merchant order creation on behalf of a customer.
 * Lifecycle transitions (accept, reject, status) are in {@link OrderLifecycleService}.
 */
@Service
@RequiredArgsConstructor
public class OrderPlacementService {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemModifierRepository orderItemModifierRepository;
    private final ModifierOptionRepository modifierOptionRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request, AuthUser authUser) {
        Customer linked = customerRepository.findByUserId(authUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No customer profile for this account"));
        PlaceOrderRequest effective = PlaceOrderRequest.builder()
                .customerId(linked.getId())
                .storeId(request.getStoreId())
                .orderType(request.getOrderType())
                .lines(request.getLines())
                .build();
        return placeOrderForCustomer(effective);
    }

    private OrderResponse placeOrderForCustomer(PlaceOrderRequest request) {
        validateCustomerActive(request.getCustomerId());
        if (!storeRepository.existsById(request.getStoreId())) {
            throw new ResourceNotFoundException("Store", request.getStoreId());
        }

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();
        for (PlaceOrderRequest.OrderLineRequest line : request.getLines()) {
            Product p = loadValidatedProduct(line.getProductId(), request.getStoreId());
            BigDecimal unit = p.getPrice() != null ? p.getPrice() : BigDecimal.ZERO;
            total = total.add(unit.multiply(BigDecimal.valueOf(line.getQuantity())));
            items.add(OrderItem.builder()
                    .productId(p.getId())
                    .productNameSnapshot(p.getName())
                    .unitPriceSnapshot(unit)
                    .quantity(line.getQuantity())
                    .build());
        }

        Order order = Order.builder()
                .storeId(request.getStoreId())
                .customerId(request.getCustomerId())
                .status(OrderStatus.NEW)
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
        validateCustomerActive(request.getCustomerId());

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();
        List<List<OrderItemModifier>> modifiersPerLine = new ArrayList<>();
        for (CreateOrderRequest.CreateOrderLineRequest line : request.getLines()) {
            Product p = loadValidatedProduct(line.getProductId(), storeId);
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
                            .price(adj)
                            .build());
                }
            }
            total = total.add(lineTotal);
            items.add(OrderItem.builder()
                    .productId(p.getId())
                    .productNameSnapshot(p.getName())
                    .unitPriceSnapshot(unit)
                    .quantity(line.getQuantity())
                    .build());
            modifiersPerLine.add(modifiers);
        }

        Order order = Order.builder()
                .storeId(storeId)
                .customerId(request.getCustomerId())
                .status(OrderStatus.NEW)
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

    /** Ensures the customer exists and is ACTIVE; throws 404/400 otherwise. */
    private void validateCustomerActive(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));
        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer account is suspended");
        }
    }

    /** Loads a product and validates it belongs to the store and is available for ordering. */
    private Product loadValidatedProduct(Long productId, Long storeId) {
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        if (p.getStoreId() == null || !p.getStoreId().equals(storeId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Product " + productId + " does not belong to this store");
        }
        if (Boolean.FALSE.equals(p.getAvailableForOrder())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Product " + productId + " is not available for order");
        }
        return p;
    }
}