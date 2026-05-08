package com.habsida.store.service;

import com.habsida.store.dto.request.CreateOrderRequest;
import com.habsida.store.dto.request.PlaceOrderRequest;
import com.habsida.store.entity.*;
import com.habsida.store.enums.CustomerStatus;
import com.habsida.store.exception.ResourceNotFoundException;
import com.habsida.store.repository.*;
import com.habsida.store.security.AuthUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Lenient strictness is needed because the save stubs in setUp() are not called
 * by tests that throw before reaching the save call.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderPlacementServiceTest {

    @Mock CustomerRepository customerRepository;
    @Mock ProductRepository productRepository;
    @Mock OrderRepository orderRepository;
    @Mock OrderItemRepository orderItemRepository;
    @Mock OrderItemModifierRepository orderItemModifierRepository;
    @Mock ModifierOptionRepository modifierOptionRepository;
    @Mock StoreRepository storeRepository;
    @InjectMocks OrderPlacementService service;

    private Customer activeCustomer;
    private Product espresso;   // $10.00
    private Product latte;      // $5.00

    @BeforeEach
    void setUp() {
        activeCustomer = new Customer();
        activeCustomer.setId(1L);
        activeCustomer.setStatus(CustomerStatus.ACTIVE);

        espresso = new Product();
        espresso.setId(10L);
        espresso.setStoreId(5L);
        espresso.setName("Espresso");
        espresso.setPrice(new BigDecimal("10.00"));
        espresso.setAvailableForOrder(true);

        latte = new Product();
        latte.setId(11L);
        latte.setStoreId(5L);
        latte.setName("Latte");
        latte.setPrice(new BigDecimal("5.00"));
        latte.setAvailableForOrder(true);

        // Capture the saved Order so findById can return the same instance.
        // createOrderForStore calls findById after save; placeOrderForCustomer does not.
        AtomicReference<Order> lastSaved = new AtomicReference<>();
        when(orderRepository.save(any())).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(100L);
            lastSaved.set(o);
            return o;
        });
        when(orderRepository.findById(100L)).thenAnswer(inv -> Optional.ofNullable(lastSaved.get()));

        when(orderItemRepository.save(any())).thenAnswer(inv -> {
            OrderItem i = inv.getArgument(0);
            i.setId(999L);
            return i;
        });
    }

    // ---- total calculation ----

    @Test
    void createOrder_twoLines_sumsToCorrectTotal() {
        when(storeRepository.existsById(5L)).thenReturn(true);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(activeCustomer));
        when(productRepository.findById(10L)).thenReturn(Optional.of(espresso));
        when(productRepository.findById(11L)).thenReturn(Optional.of(latte));

        var request = CreateOrderRequest.builder()
                .customerId(1L)
                .lines(List.of(
                        line(10L, 2, null),   // 10.00 × 2 = 20.00
                        line(11L, 3, null)    //  5.00 × 3 = 15.00
                ))
                .build();

        var response = service.createOrderForStore(5L, request);

        assertThat(response.getTotalAmount()).isEqualByComparingTo("35.00");
    }

    @Test
    void createOrder_withModifier_addsAdjustmentPerItem() {
        ModifierOption extraShot = new ModifierOption();
        extraShot.setId(20L);
        extraShot.setName("Extra shot");
        extraShot.setPriceAdjustment(new BigDecimal("2.00"));

        when(storeRepository.existsById(5L)).thenReturn(true);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(activeCustomer));
        when(productRepository.findById(10L)).thenReturn(Optional.of(espresso));
        when(modifierOptionRepository.findById(20L)).thenReturn(Optional.of(extraShot));

        // (10.00 + 2.00) × 3 = 36.00
        var request = CreateOrderRequest.builder()
                .customerId(1L)
                .lines(List.of(line(10L, 3, List.of(20L))))
                .build();

        var response = service.createOrderForStore(5L, request);

        assertThat(response.getTotalAmount()).isEqualByComparingTo("36.00");
    }

    @Test
    void createOrder_nullModifierAdjustment_treatedAsZero() {
        ModifierOption freeNote = new ModifierOption();
        freeNote.setId(21L);
        freeNote.setName("No sugar");
        freeNote.setPriceAdjustment(null);

        when(storeRepository.existsById(5L)).thenReturn(true);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(activeCustomer));
        when(productRepository.findById(10L)).thenReturn(Optional.of(espresso));
        when(modifierOptionRepository.findById(21L)).thenReturn(Optional.of(freeNote));

        // 10.00 × 2 = 20.00 (null adjustment treated as zero)
        var request = CreateOrderRequest.builder()
                .customerId(1L)
                .lines(List.of(line(10L, 2, List.of(21L))))
                .build();

        var response = service.createOrderForStore(5L, request);
        assertThat(response.getTotalAmount()).isEqualByComparingTo("20.00");
    }

    // ---- validation ----

    @Test
    void createOrder_suspendedCustomer_throwsBadRequest() {
        Customer suspended = new Customer();
        suspended.setId(1L);
        suspended.setStatus(CustomerStatus.SUSPENDED);

        when(storeRepository.existsById(5L)).thenReturn(true);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(suspended));

        assertThatThrownBy(() -> service.createOrderForStore(5L, singleLineRequest()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("suspended");
    }

    @Test
    void createOrder_productBelongsToWrongStore_throwsBadRequest() {
        Product foreign = new Product();
        foreign.setId(10L);
        foreign.setStoreId(99L);
        foreign.setName("x");
        foreign.setPrice(BigDecimal.ONE);
        foreign.setAvailableForOrder(true);

        when(storeRepository.existsById(5L)).thenReturn(true);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(activeCustomer));
        when(productRepository.findById(10L)).thenReturn(Optional.of(foreign));

        assertThatThrownBy(() -> service.createOrderForStore(5L, singleLineRequest()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("does not belong to this store");
    }

    @Test
    void createOrder_unavailableProduct_throwsBadRequest() {
        Product paused = new Product();
        paused.setId(10L);
        paused.setStoreId(5L);
        paused.setName("x");
        paused.setPrice(BigDecimal.ONE);
        paused.setAvailableForOrder(false);

        when(storeRepository.existsById(5L)).thenReturn(true);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(activeCustomer));
        when(productRepository.findById(10L)).thenReturn(Optional.of(paused));

        assertThatThrownBy(() -> service.createOrderForStore(5L, singleLineRequest()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not available for order");
    }

    @Test
    void createOrder_storeNotFound_throws404() {
        when(storeRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.createOrderForStore(99L, singleLineRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---- placeOrder ----

    @Test
    void placeOrder_linksAuthenticatedCustomerAndCalculatesTotal() {
        User user = new User();
        user.setId(42L);
        user.setEmail("user@example.com");
        AuthUser authUser = new AuthUser(user, List.of());

        when(customerRepository.findByUserId(42L)).thenReturn(Optional.of(activeCustomer));
        when(storeRepository.existsById(5L)).thenReturn(true);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(activeCustomer));
        when(productRepository.findById(10L)).thenReturn(Optional.of(espresso));

        var request = PlaceOrderRequest.builder()
                .storeId(5L)
                .lines(List.of(PlaceOrderRequest.OrderLineRequest.builder()
                        .productId(10L).quantity(2).build()))
                .build();

        var response = service.placeOrder(request, authUser);

        assertThat(response.getTotalAmount()).isEqualByComparingTo("20.00");
        assertThat(response.getCustomerId()).isEqualTo(1L);
    }

    @Test
    void placeOrder_noCustomerProfile_throwsForbidden() {
        User user = new User();
        user.setId(42L);
        user.setEmail("orphan@example.com");
        AuthUser authUser = new AuthUser(user, List.of());

        when(customerRepository.findByUserId(42L)).thenReturn(Optional.empty());

        var request = PlaceOrderRequest.builder()
                .storeId(5L)
                .lines(List.of(PlaceOrderRequest.OrderLineRequest.builder()
                        .productId(10L).quantity(1).build()))
                .build();

        assertThatThrownBy(() -> service.placeOrder(request, authUser))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No customer profile");
    }

    // ---- helpers ----

    private static CreateOrderRequest.CreateOrderLineRequest line(Long productId, int qty, List<Long> modifierIds) {
        return CreateOrderRequest.CreateOrderLineRequest.builder()
                .productId(productId)
                .quantity(qty)
                .modifierOptionIds(modifierIds)
                .build();
    }

    private static CreateOrderRequest singleLineRequest() {
        return CreateOrderRequest.builder()
                .customerId(1L)
                .lines(List.of(line(10L, 1, null)))
                .build();
    }
}
