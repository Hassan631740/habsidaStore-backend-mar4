package com.habsida.store.service;

import com.habsida.store.entity.Order;
import com.habsida.store.enums.OrderStatus;
import com.habsida.store.exception.ResourceNotFoundException;
import com.habsida.store.repository.OrderItemRepository;
import com.habsida.store.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderLifecycleServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock OrderItemRepository orderItemRepository;
    @InjectMocks OrderLifecycleService service;

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(1L);
        order.setStoreId(10L);
        order.setStatus(OrderStatus.NEW);
    }

    // ---- merchantAccept ----

    @Test
    void accept_newOrder_transitionsToAccepted() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var r = service.merchantAccept(1L, List.of(10L));

        assertThat(r.getStatus()).isEqualTo(OrderStatus.ACCEPTED);
        assertThat(order.getAcceptedAt()).isNotNull();
        assertThat(order.getRejectedAt()).isNull();
    }

    @Test
    void accept_pendingOrder_transitionsToAccepted() {
        order.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.merchantAccept(1L, List.of(10L)).getStatus()).isEqualTo(OrderStatus.ACCEPTED);
    }

    @Test
    void accept_alreadyAccepted_throwsBadRequest() {
        order.setStatus(OrderStatus.ACCEPTED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.merchantAccept(1L, List.of(10L)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Only NEW or PENDING");
    }

    @Test
    void accept_emptyStoreList_throwsForbidden() {
        assertThatThrownBy(() -> service.merchantAccept(1L, List.of()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("no assigned stores");
    }

    @Test
    void accept_orderNotFound_throws404() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.merchantAccept(99L, List.of(10L)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void accept_wrongStore_throwsForbidden() {
        // order.storeId = 10, merchant only assigned to store 99
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderItemRepository.countByOrderId(1L)).thenReturn(3L);
        when(orderRepository.countOrderItemsInStores(1L, List.of(99L))).thenReturn(0L);

        assertThatThrownBy(() -> service.merchantAccept(1L, List.of(99L)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not fully fulfilled");
    }

    @Test
    void accept_orderWithNoItems_throwsBadRequest() {
        // storeId doesn't match → falls through to item count check
        order.setStoreId(null);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderItemRepository.countByOrderId(1L)).thenReturn(0L);

        assertThatThrownBy(() -> service.merchantAccept(1L, List.of(10L)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("no line items");
    }

    // ---- merchantReject ----

    @Test
    void reject_newOrder_transitionsToRejected() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var r = service.merchantReject(1L, List.of(10L), "Out of stock");

        assertThat(r.getStatus()).isEqualTo(OrderStatus.REJECTED);
        assertThat(order.getRejectedAt()).isNotNull();
        assertThat(order.getRejectReason()).isEqualTo("Out of stock");
        assertThat(order.getAcceptedAt()).isNull();
    }

    @Test
    void reject_acceptedOrder_throwsBadRequest() {
        order.setStatus(OrderStatus.ACCEPTED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.merchantReject(1L, List.of(10L), "reason"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Only NEW or PENDING");
    }

    // ---- updateStatusAfterAcceptance (transition table) ----

    @Test
    void transition_acceptedToInProgress_ok() {
        order.setStatus(OrderStatus.ACCEPTED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.updateStatusAfterAcceptance(1L, OrderStatus.IN_PROGRESS, List.of(10L)).getStatus())
                .isEqualTo(OrderStatus.IN_PROGRESS);
    }

    @Test
    void transition_inProgressToCompleted_ok() {
        order.setStatus(OrderStatus.IN_PROGRESS);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.updateStatusAfterAcceptance(1L, OrderStatus.COMPLETED, List.of(10L)).getStatus())
                .isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    void transition_acceptedToCanceled_setsRejectedAt() {
        order.setStatus(OrderStatus.ACCEPTED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var r = service.updateStatusAfterAcceptance(1L, OrderStatus.CANCELED, List.of(10L));

        assertThat(r.getStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThat(order.getRejectedAt()).isNotNull();
    }

    @Test
    void transition_newToCanceled_ok() {
        order.setStatus(OrderStatus.NEW);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.updateStatusAfterAcceptance(1L, OrderStatus.CANCELED, List.of(10L)).getStatus())
                .isEqualTo(OrderStatus.CANCELED);
    }

    @Test
    void transition_newToCompleted_invalid() {
        order.setStatus(OrderStatus.NEW);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.updateStatusAfterAcceptance(1L, OrderStatus.COMPLETED, List.of(10L)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Cannot move from");
    }

    @Test
    void transition_completedToInProgress_invalid() {
        order.setStatus(OrderStatus.COMPLETED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.updateStatusAfterAcceptance(1L, OrderStatus.IN_PROGRESS, List.of(10L)))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void transition_canceledOrder_cannotTransitionFurther() {
        order.setStatus(OrderStatus.CANCELED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.updateStatusAfterAcceptance(1L, OrderStatus.IN_PROGRESS, List.of(10L)))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void transition_rejectedOrder_cannotTransitionFurther() {
        order.setStatus(OrderStatus.REJECTED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.updateStatusAfterAcceptance(1L, OrderStatus.ACCEPTED, List.of(10L)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Cannot move from");
    }
}