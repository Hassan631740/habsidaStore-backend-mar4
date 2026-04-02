package com.habsida.store.dto;

import com.habsida.store.dto.request.*;
import com.habsida.store.dto.response.*;
import com.habsida.store.entity.*;
import com.habsida.store.enums.*;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Maps between entities and Request/Response DTOs.
 * For Store: after toEntity(StoreRequest), set entity.setAddress(addressRepo.getReferenceById(request.getAddressId())) before save.
 */
public final class DtoMapper {

    private DtoMapper() {}

    private static StoreStatus safeStoreStatus(String v) {
        if (v == null || v.isBlank()) return null;
        try { return StoreStatus.valueOf(v); } catch (IllegalArgumentException e) { return null; }
    }
    /**
     * Maps persisted legacy {@link OrderStatus} values to the canonical lifecycle shown in API responses.
     */
    private static OrderStatus orderStatusForResponse(OrderStatus v) {
        if (v == null) {
            return null;
        }
        return switch (v) {
            case PENDING -> OrderStatus.NEW;
            case CONFIRMED -> OrderStatus.ACCEPTED;
            case PROCESSING, READY, SHIPPED -> OrderStatus.IN_PROGRESS;
            case DELIVERED -> OrderStatus.COMPLETED;
            case CANCELLED -> OrderStatus.CANCELED;
            default -> v;
        };
    }
    private static OrderType safeOrderType(String v) {
        if (v == null || v.isBlank()) return null;
        try { return OrderType.valueOf(v); } catch (IllegalArgumentException e) { return null; }
    }
    private static PaymentMethod safePaymentMethod(String v) {
        if (v == null || v.isBlank()) return null;
        try { return PaymentMethod.valueOf(v); } catch (IllegalArgumentException e) { return null; }
    }
    private static PaymentStatus safePaymentStatus(String v) {
        if (v == null || v.isBlank()) return null;
        try { return PaymentStatus.valueOf(v); } catch (IllegalArgumentException e) { return null; }
    }
    // ---------- Address ----------
    public static AddressResponse toResponse(Address e) {
        if (e == null) return null;
        return AddressResponse.builder()
                .id(e.getId())
                .streetLine1(e.getStreetLine1())
                .streetLine2(e.getStreetLine2())
                .city(e.getCity())
                .state(e.getState())
                .postalCode(e.getPostalCode())
                .country(e.getCountry())
                .build();
    }

    public static Address toEntity(AddressRequest r) {
        if (r == null) return null;
        Address e = new Address();
        e.setStreetLine1(r.getStreetLine1());
        e.setStreetLine2(r.getStreetLine2());
        e.setCity(r.getCity());
        e.setState(r.getState());
        e.setPostalCode(r.getPostalCode());
        e.setCountry(r.getCountry());
        return e;
    }

    // ---------- Cart ----------
    public static CartResponse toResponse(Cart e) {
        if (e == null) return null;
        return CartResponse.builder()
                .id(e.getId())
                .customerId(e.getCustomerId())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    public static Cart toEntity(CartRequest r) {
        if (r == null) return null;
        Cart e = new Cart();
        e.setCustomerId(r.getCustomerId());
        return e;
    }

    // ---------- CartItem ----------
    public static CartItemResponse toResponse(CartItem e) {
        if (e == null) return null;
        return CartItemResponse.builder()
                .id(e.getId())
                .cartId(e.getCartId())
                .productId(e.getProductId())
                .quantity(e.getQuantity())
                .build();
    }

    public static CartItem toEntity(CartItemRequest r) {
        if (r == null) return null;
        CartItem e = new CartItem();
        e.setCartId(r.getCartId());
        e.setProductId(r.getProductId());
        e.setQuantity(r.getQuantity());
        return e;
    }

    // ---------- Category ----------
    public static CategoryResponse toResponse(Category e) {
        if (e == null) return null;
        return CategoryResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .slug(e.getSlug())
                .parentId(e.getParentId())
                .storeId(e.getStoreId())
                .build();
    }

    public static Category toEntity(CategoryRequest r) {
        if (r == null) return null;
        Category e = new Category();
        e.setName(r.getName());
        e.setSlug(r.getSlug());
        e.setStoreId(r.getStoreId());
        e.setParentId(r.getParentId());
        return e;
    }

    // ---------- Customer ----------
    /** API-facing status when the persisted field is null (defaults to ACTIVE). */
    public static CustomerStatus customerStatusForResponse(CustomerStatus status) {
        return status != null ? status : CustomerStatus.ACTIVE;
    }

    public static CustomerResponse toResponse(Customer e) {
        if (e == null) return null;
        return CustomerResponse.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .phone(e.getPhone())
                .status(customerStatusForResponse(e.getStatus()))
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    public static Customer toEntity(CustomerRequest r) {
        if (r == null) return null;
        Customer e = new Customer();
        e.setUserId(r.getUserId());
        e.setFirstName(r.getFirstName());
        e.setLastName(r.getLastName());
        e.setPhone(r.getPhone());
        e.setStatus(customerStatusForResponse(r.getStatus()));
        return e;
    }

    // ---------- CustomerAddress ----------
    public static CustomerAddressResponse toResponse(CustomerAddress e) {
        if (e == null) return null;
        return CustomerAddressResponse.builder()
                .id(e.getId())
                .customerId(e.getCustomerId())
                .addressId(e.getAddressId())
                .build();
    }

    public static CustomerAddress toEntity(CustomerAddressRequest r) {
        if (r == null) return null;
        CustomerAddress e = new CustomerAddress();
        e.setCustomerId(r.getCustomerId());
        e.setAddressId(r.getAddressId());
        return e;
    }

    // ---------- Fulfillment ----------
    public static FulfillmentResponse toResponse(Fulfillment e) {
        if (e == null) return null;
        return FulfillmentResponse.builder()
                .id(e.getId())
                .orderId(e.getOrderId())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    public static Fulfillment toEntity(FulfillmentRequest r) {
        if (r == null) return null;
        Fulfillment e = new Fulfillment();
        e.setOrderId(r.getOrderId());
        e.setStatus(r.getStatus());
        return e;
    }

    // ---------- ModifierGroup ----------
    public static ModifierGroupResponse toResponse(ModifierGroup e) {
        if (e == null) return null;
        return ModifierGroupResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .storeId(e.getStoreId())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    public static ModifierGroup toEntity(ModifierGroupRequest r) {
        if (r == null) return null;
        ModifierGroup e = new ModifierGroup();
        e.setName(r.getName());
        e.setStoreId(r.getStoreId());
        return e;
    }

    // ---------- ModifierOption ----------
    public static ModifierOptionResponse toResponse(ModifierOption e) {
        if (e == null) return null;
        return ModifierOptionResponse.builder()
                .id(e.getId())
                .modifierGroupId(e.getModifierGroupId())
                .name(e.getName())
                .priceAdjustment(e.getPriceAdjustment())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    public static ModifierOption toEntity(ModifierOptionRequest r) {
        if (r == null) return null;
        ModifierOption e = new ModifierOption();
        e.setModifierGroupId(r.getModifierGroupId());
        e.setName(r.getName());
        e.setPriceAdjustment(r.getPriceAdjustment());
        return e;
    }

    // ---------- Order ----------
    public static OrderResponse toResponse(Order e) {
        if (e == null) return null;
        return OrderResponse.builder()
                .id(e.getId())
                .storeId(e.getStoreId())
                .customerId(e.getCustomerId())
                .status(orderStatusForResponse(e.getStatus()))
                .orderType(safeOrderType(e.getOrderType()))
                .totalAmount(e.getTotalAmount())
                .acceptedAt(e.getAcceptedAt())
                .rejectedAt(e.getRejectedAt())
                .rejectReason(e.getRejectReason())
                .notes(e.getNotes())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    public static Order toEntity(OrderRequest r) {
        if (r == null) return null;
        Order e = new Order();
        e.setStoreId(r.getStoreId());
        e.setCustomerId(r.getCustomerId());
        e.setStatus(r.getStatus());
        e.setOrderType(r.getOrderType() != null ? r.getOrderType().name() : null);
        e.setTotalAmount(r.getTotalAmount());
        e.setNotes(r.getNotes());
        return e;
    }

    // ---------- OrderAddress ----------
    public static OrderAddressResponse toResponse(OrderAddress e) {
        if (e == null) return null;
        return OrderAddressResponse.builder()
                .id(e.getId())
                .orderId(e.getOrderId())
                .addressId(e.getAddressId())
                .build();
    }

    public static OrderAddress toEntity(OrderAddressRequest r) {
        if (r == null) return null;
        OrderAddress e = new OrderAddress();
        e.setOrderId(r.getOrderId());
        e.setAddressId(r.getAddressId());
        return e;
    }

    // ---------- OrderItem ----------
    public static OrderItemResponse toResponse(OrderItem e) {
        if (e == null) return null;
        return OrderItemResponse.builder()
                .id(e.getId())
                .orderId(e.getOrderId())
                .productId(e.getProductId())
                .productNameSnapshot(e.getProductNameSnapshot())
                .unitPriceSnapshot(e.getUnitPriceSnapshot())
                .quantity(e.getQuantity())
                .price(e.getUnitPriceSnapshot())
                .build();
    }

    public static OrderItem toEntity(OrderItemRequest r) {
        if (r == null) return null;
        OrderItem e = new OrderItem();
        e.setOrderId(r.getOrderId());
        e.setProductId(r.getProductId());
        e.setProductNameSnapshot(r.getProductNameSnapshot());
        e.setUnitPriceSnapshot(r.getUnitPriceSnapshot());
        e.setQuantity(r.getQuantity());
        e.setPrice(r.getPrice());
        return e;
    }

    // ---------- OrderItemModifier ----------
    public static OrderItemModifierResponse toResponse(OrderItemModifier e) {
        if (e == null) return null;
        return OrderItemModifierResponse.builder()
                .id(e.getId())
                .orderItemId(e.getOrderItemId())
                .modifierOptionId(e.getModifierOptionId())
                .optionNameSnapshot(e.getOptionNameSnapshot())
                .price(e.getPrice())
                .build();
    }

    public static OrderItemModifier toEntity(OrderItemModifierRequest r) {
        if (r == null) return null;
        OrderItemModifier e = new OrderItemModifier();
        e.setOrderItemId(r.getOrderItemId());
        e.setModifierOptionId(r.getModifierOptionId());
        e.setOptionNameSnapshot(r.getOptionNameSnapshot());
        e.setPrice(r.getPrice());
        return e;
    }

    // ---------- OrderPayment ----------
    public static OrderPaymentResponse toResponse(OrderPayment e) {
        if (e == null) return null;
        return OrderPaymentResponse.builder()
                .id(e.getId())
                .orderId(e.getOrderId())
                .amount(e.getAmount())
                .paymentMethod(safePaymentMethod(e.getPaymentMethod()))
                .status(safePaymentStatus(e.getStatus()))
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    public static OrderPayment toEntity(OrderPaymentRequest r) {
        if (r == null) return null;
        OrderPayment e = new OrderPayment();
        e.setOrderId(r.getOrderId());
        e.setAmount(r.getAmount());
        e.setPaymentMethod(r.getPaymentMethod() != null ? r.getPaymentMethod().name() : null);
        e.setStatus(r.getStatus() != null ? r.getStatus().name() : null);
        return e;
    }

    // ---------- Product ----------
    public static ProductResponse toResponse(Product e) {
        if (e == null) return null;
        return ProductResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .price(e.getPrice())
                .categoryId(e.getCategoryId())
                .storeId(e.getStoreId())
                .availableForOrder(e.getAvailableForOrder() != null ? e.getAvailableForOrder() : true)
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    public static Product toEntity(ProductRequest r) {
        if (r == null) return null;
        Product e = new Product();
        e.setName(r.getName());
        e.setDescription(r.getDescription());
        e.setPrice(r.getPrice());
        e.setCategoryId(r.getCategoryId());
        e.setStoreId(r.getStoreId());
        e.setAvailableForOrder(r.getAvailableForOrder() != null ? r.getAvailableForOrder() : true);
        return e;
    }

    // ---------- ProductImage ----------
    public static ProductImageResponse toResponse(ProductImage e) {
        if (e == null) return null;
        return ProductImageResponse.builder()
                .id(e.getId())
                .productId(e.getProductId())
                .url(e.getUrl())
                .sortOrder(e.getSortOrder())
                .build();
    }

    public static ProductImage toEntity(ProductImageRequest r) {
        if (r == null) return null;
        ProductImage e = new ProductImage();
        e.setProductId(r.getProductId());
        e.setUrl(r.getUrl());
        e.setSortOrder(r.getSortOrder());
        return e;
    }

    // ---------- ProductModifierGroup ----------
    public static ProductModifierGroupResponse toResponse(ProductModifierGroup e) {
        if (e == null) return null;
        return ProductModifierGroupResponse.builder()
                .id(e.getId())
                .productId(e.getProductId())
                .modifierGroupId(e.getModifierGroupId())
                .build();
    }

    public static ProductModifierGroup toEntity(ProductModifierGroupRequest r) {
        if (r == null) return null;
        ProductModifierGroup e = new ProductModifierGroup();
        e.setProductId(r.getProductId());
        e.setModifierGroupId(r.getModifierGroupId());
        return e;
    }

    // ---------- Role ----------
    public static RoleResponse toResponse(Role e) {
        if (e == null) return null;
        return RoleResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .build();
    }

    public static Role toEntity(RoleRequest r) {
        if (r == null) return null;
        Role e = new Role();
        e.setName(r.getName());
        return e;
    }

    // ---------- Store ----------
    // After toEntity(StoreRequest), set entity.setAddress(addressRepo.getReferenceById(request.getAddressId())) before save.
    public static StoreResponse toResponse(Store e) {
        if (e == null) return null;
        Long addressId = e.getAddress() != null ? e.getAddress().getId() : null;
        StoreStatus status = safeStoreStatus(e.getStatus());
        return StoreResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .addressId(addressId)
                .status(status)
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    public static Store toEntity(StoreRequest r) {
        if (r == null) return null;
        Store e = new Store();
        e.setName(r.getName());
        e.setStatus(r.getStatus() != null ? r.getStatus().name() : null);
        return e;
    }

    // ---------- StoreBreaks ----------
    public static StoreBreaksResponse toResponse(StoreBreaks e) {
        if (e == null) return null;
        return StoreBreaksResponse.builder()
                .id(e.getId())
                .storeId(e.getStoreId())
                .startTime(e.getStartTime())
                .endTime(e.getEndTime())
                .build();
    }

    public static StoreBreaks toEntity(StoreBreaksRequest r) {
        if (r == null) return null;
        StoreBreaks e = new StoreBreaks();
        e.setStoreId(r.getStoreId());
        e.setStartTime(r.getStartTime());
        e.setEndTime(r.getEndTime());
        return e;
    }

    // ---------- StoreDeliveryArea ----------
    public static StoreDeliveryAreaResponse toResponse(StoreDeliveryArea e) {
        if (e == null) return null;
        return StoreDeliveryAreaResponse.builder()
                .id(e.getId())
                .storeId(e.getStoreId())
                .name(e.getName())
                .build();
    }

    public static StoreDeliveryArea toEntity(StoreDeliveryAreaRequest r) {
        if (r == null) return null;
        StoreDeliveryArea e = new StoreDeliveryArea();
        e.setStoreId(r.getStoreId());
        e.setName(r.getName());
        return e;
    }

    // ---------- StoreDeliveryRestriction ----------
    public static StoreDeliveryRestrictionResponse toResponse(StoreDeliveryRestriction e) {
        if (e == null) return null;
        return StoreDeliveryRestrictionResponse.builder()
                .id(e.getId())
                .storeId(e.getStoreId())
                .type(e.getType())
                .value(e.getValue())
                .build();
    }

    public static StoreDeliveryRestriction toEntity(StoreDeliveryRestrictionRequest r) {
        if (r == null) return null;
        StoreDeliveryRestriction e = new StoreDeliveryRestriction();
        e.setStoreId(r.getStoreId());
        e.setType(r.getType());
        e.setValue(r.getValue());
        return e;
    }

    // ---------- StoreDeliverySettings ----------
    public static StoreDeliverySettingsResponse toResponse(StoreDeliverySettings e) {
        if (e == null) return null;
        return StoreDeliverySettingsResponse.builder()
                .id(e.getId())
                .storeId(e.getStoreId())
                .deliveryFee(e.getDeliveryFee())
                .minOrderAmount(e.getMinOrderAmount())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    public static StoreDeliverySettings toEntity(StoreDeliverySettingsRequest r) {
        if (r == null) return null;
        StoreDeliverySettings e = new StoreDeliverySettings();
        e.setStoreId(r.getStoreId());
        e.setDeliveryFee(r.getDeliveryFee());
        e.setMinOrderAmount(r.getMinOrderAmount());
        return e;
    }

    // ---------- StoreHours ----------
    public static StoreHoursResponse toResponse(StoreHours e) {
        if (e == null) return null;
        return StoreHoursResponse.builder()
                .id(e.getId())
                .storeId(e.getStoreId())
                .dayOfWeek(e.getDayOfWeek())
                .openTime(e.getOpenTime())
                .closeTime(e.getCloseTime())
                .build();
    }

    public static StoreHours toEntity(StoreHoursRequest r) {
        if (r == null) return null;
        StoreHours e = new StoreHours();
        e.setStoreId(r.getStoreId());
        e.setDayOfWeek(r.getDayOfWeek());
        e.setOpenTime(r.getOpenTime());
        e.setCloseTime(r.getCloseTime());
        return e;
    }

    // ---------- User ----------
    public static UserResponse toResponse(User e) {
        if (e == null) return null;
        return UserResponse.builder()
                .id(e.getId())
                .email(e.getEmail())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    public static User toEntity(UserRequest r, PasswordEncoder passwordEncoder) {
        if (r == null) return null;
        User e = new User();
        e.setEmail(r.getEmail());
        if (r.getPassword() != null && !r.getPassword().isBlank()) {
            e.setPasswordHash(passwordEncoder.encode(r.getPassword()));
        }
        return e;
    }

    /**
     * For update: only set email (password unchanged if not provided).
     */
    public static void updateEntity(User entity, UserRequest r, PasswordEncoder passwordEncoder) {
        if (entity == null || r == null) return;
        entity.setEmail(r.getEmail());
        if (r.getPassword() != null && !r.getPassword().isBlank()) {
            entity.setPasswordHash(passwordEncoder.encode(r.getPassword()));
        }
    }

    // ---------- UserRole ----------
    public static UserRoleResponse toResponse(UserRole e) {
        if (e == null) return null;
        return UserRoleResponse.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .roleId(e.getRoleId())
                .build();
    }

    public static UserRole toEntity(UserRoleRequest r) {
        if (r == null) return null;
        UserRole e = new UserRole();
        e.setUserId(r.getUserId());
        e.setRoleId(r.getRoleId());
        return e;
    }

    // ---------- UserStoreAccess ----------
    public static UserStoreAccessResponse toResponse(UserStoreAccess e) {
        if (e == null) return null;
        return UserStoreAccessResponse.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .storeId(e.getStoreId())
                .build();
    }

    public static UserStoreAccess toEntity(UserStoreAccessRequest r) {
        if (r == null) return null;
        UserStoreAccess e = new UserStoreAccess();
        e.setUserId(r.getUserId());
        e.setStoreId(r.getStoreId());
        return e;
    }
}
