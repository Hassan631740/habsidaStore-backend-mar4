package com.habsida.store.integration;

import com.habsida.store.dto.request.CreateOrderRequest;
import com.habsida.store.dto.response.OrderResponse;
import com.habsida.store.entity.*;
import com.habsida.store.enums.CustomerStatus;
import com.habsida.store.enums.OrderStatus;
import com.habsida.store.repository.*;
import com.habsida.store.service.OrderPlacementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class OrderPlacementIntegrationTest extends AbstractIntegrationTest {

    @Autowired OrderPlacementService placementService;
    @Autowired StoreRepository storeRepository;
    @Autowired UserRepository userRepository;
    @Autowired CustomerRepository customerRepository;
    @Autowired ProductRepository productRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired OrderItemRepository orderItemRepository;

    private Store store;
    private Customer customer;
    private Product product1;
    private Product product2;

    @BeforeEach
    void setup() {
        store = storeRepository.save(Store.builder().name("Test Café").status("ACTIVE").build());

        User user = new User();
        user.setEmail("customer@integration.test");
        user = userRepository.save(user);

        customer = new Customer();
        customer.setUserId(user.getId());
        customer.setStatus(CustomerStatus.ACTIVE);
        customer = customerRepository.save(customer);

        product1 = new Product();
        product1.setStoreId(store.getId());
        product1.setName("Espresso");
        product1.setPrice(new BigDecimal("10.00"));
        product1.setAvailableForOrder(true);
        product1 = productRepository.save(product1);

        product2 = new Product();
        product2.setStoreId(store.getId());
        product2.setName("Latte");
        product2.setPrice(new BigDecimal("5.00"));
        product2.setAvailableForOrder(true);
        product2 = productRepository.save(product2);
    }

    @Test
    void createOrder_persistsOrderWithCorrectTotal() {
        var request = CreateOrderRequest.builder()
                .customerId(customer.getId())
                .lines(List.of(
                        line(product1.getId(), 2),   // 10.00 × 2 = 20.00
                        line(product2.getId(), 3)    //  5.00 × 3 = 15.00
                ))
                .build();

        OrderResponse response = placementService.createOrderForStore(store.getId(), request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getTotalAmount()).isEqualByComparingTo("35.00");
        assertThat(response.getStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(response.getStoreId()).isEqualTo(store.getId());
        assertThat(response.getCustomerId()).isEqualTo(customer.getId());
    }

    @Test
    void createOrder_persistsLineItemsInDatabase() {
        var request = CreateOrderRequest.builder()
                .customerId(customer.getId())
                .lines(List.of(
                        line(product1.getId(), 1),
                        line(product2.getId(), 2)
                ))
                .build();

        OrderResponse response = placementService.createOrderForStore(store.getId(), request);

        var items = orderItemRepository.findByOrderId(response.getId());
        assertThat(items).hasSize(2);
        assertThat(items).extracting(OrderItem::getProductId)
                .containsExactlyInAnyOrder(product1.getId(), product2.getId());
    }

    @Test
    void createOrder_snapshotsProductNameAndPrice() {
        var request = CreateOrderRequest.builder()
                .customerId(customer.getId())
                .lines(List.of(line(product1.getId(), 1)))
                .build();

        placementService.createOrderForStore(store.getId(), request);

        // Verify the snapshot was captured independent of the live product record
        var items = orderItemRepository.findAll().stream()
                .filter(i -> product1.getId().equals(i.getProductId()))
                .toList();
        assertThat(items).isNotEmpty();
        assertThat(items.get(0).getProductNameSnapshot()).isEqualTo("Espresso");
        assertThat(items.get(0).getUnitPriceSnapshot()).isEqualByComparingTo("10.00");
    }

    private static CreateOrderRequest.CreateOrderLineRequest line(Long productId, int qty) {
        return CreateOrderRequest.CreateOrderLineRequest.builder()
                .productId(productId)
                .quantity(qty)
                .build();
    }
}