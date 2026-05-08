package com.habsida.store.integration;

import com.habsida.store.dto.request.CreateOrderRequest;
import com.habsida.store.entity.*;
import com.habsida.store.enums.CustomerStatus;
import com.habsida.store.enums.OrderStatus;
import com.habsida.store.repository.*;
import com.habsida.store.service.OrderLifecycleService;
import com.habsida.store.service.OrderPlacementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Security tests: verifies that a merchant cannot read or modify orders belonging to another store.
 */
@Transactional
class MerchantIsolationTest extends AbstractIntegrationTest {

    @Autowired OrderPlacementService placementService;
    @Autowired OrderLifecycleService lifecycleService;
    @Autowired StoreRepository storeRepository;
    @Autowired UserRepository userRepository;
    @Autowired CustomerRepository customerRepository;
    @Autowired ProductRepository productRepository;
    @Autowired UserStoreAccessRepository userStoreAccessRepository;
    @Autowired OrderRepository orderRepository;

    private Long storeAId;
    private Long storeBId;
    private Long orderAId;
    private Long orderBId;

    @BeforeEach
    void setup() {
        // Two independent stores
        Store storeA = storeRepository.save(Store.builder().name("Store A").status("ACTIVE").build());
        Store storeB = storeRepository.save(Store.builder().name("Store B").status("ACTIVE").build());
        storeAId = storeA.getId();
        storeBId = storeB.getId();

        // Two merchants, each linked to their own store only
        User merchantUserA = new User();
        merchantUserA.setEmail("merchantA@test.com");
        merchantUserA = userRepository.save(merchantUserA);
        userStoreAccessRepository.save(UserStoreAccess.builder()
                .userId(merchantUserA.getId()).storeId(storeAId).build());

        User merchantUserB = new User();
        merchantUserB.setEmail("merchantB@test.com");
        merchantUserB = userRepository.save(merchantUserB);
        userStoreAccessRepository.save(UserStoreAccess.builder()
                .userId(merchantUserB.getId()).storeId(storeBId).build());

        // A shared customer
        User customerUser = new User();
        customerUser.setEmail("shared.customer@test.com");
        customerUser = userRepository.save(customerUser);
        Customer customer = new Customer();
        customer.setUserId(customerUser.getId());
        customer.setStatus(CustomerStatus.ACTIVE);
        customer = customerRepository.save(customer);

        // One product per store
        Product productA = new Product();
        productA.setStoreId(storeAId);
        productA.setName("Product A");
        productA.setPrice(new BigDecimal("10.00"));
        productA.setAvailableForOrder(true);
        productA = productRepository.save(productA);

        Product productB = new Product();
        productB.setStoreId(storeBId);
        productB.setName("Product B");
        productB.setPrice(new BigDecimal("20.00"));
        productB.setAvailableForOrder(true);
        productB = productRepository.save(productB);

        // One order per store
        var orderAResponse = placementService.createOrderForStore(storeAId, CreateOrderRequest.builder()
                .customerId(customer.getId())
                .lines(List.of(CreateOrderRequest.CreateOrderLineRequest.builder()
                        .productId(productA.getId()).quantity(1).build()))
                .build());
        orderAId = orderAResponse.getId();

        var orderBResponse = placementService.createOrderForStore(storeBId, CreateOrderRequest.builder()
                .customerId(customer.getId())
                .lines(List.of(CreateOrderRequest.CreateOrderLineRequest.builder()
                        .productId(productB.getId()).quantity(1).build()))
                .build());
        orderBId = orderBResponse.getId();
    }

    @Test
    void merchantA_cannotAcceptOrderBelongingToStoreB() {
        assertThatThrownBy(() -> lifecycleService.merchantAccept(orderBId, List.of(storeAId)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void merchantA_cannotRejectOrderBelongingToStoreB() {
        assertThatThrownBy(() -> lifecycleService.merchantReject(orderBId, List.of(storeAId), "no reason"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void merchantA_cannotUpdateStatusOfOrderBelongingToStoreB() {
        // First accept orderB as merchantB so it's in ACCEPTED state
        lifecycleService.merchantAccept(orderBId, List.of(storeBId));

        // Now merchantA tries to advance orderB to IN_PROGRESS
        assertThatThrownBy(() ->
                lifecycleService.updateStatusAfterAcceptance(orderBId, OrderStatus.IN_PROGRESS, List.of(storeAId)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void storeQuery_doesNotLeakCrossStoreOrders() {
        var pageA = orderRepository.findByStoreIds(List.of(storeAId), PageRequest.of(0, 20));
        var pageB = orderRepository.findByStoreIds(List.of(storeBId), PageRequest.of(0, 20));

        var idsForA = pageA.getContent().stream().map(Order::getId).toList();
        var idsForB = pageB.getContent().stream().map(Order::getId).toList();

        assertThat(idsForA).contains(orderAId).doesNotContain(orderBId);
        assertThat(idsForB).contains(orderBId).doesNotContain(orderAId);
    }

    @Test
    void merchantA_canAcceptTheirOwnOrder() {
        var response = lifecycleService.merchantAccept(orderAId, List.of(storeAId));
        assertThat(response.getStatus()).isEqualTo(OrderStatus.ACCEPTED);
    }
}