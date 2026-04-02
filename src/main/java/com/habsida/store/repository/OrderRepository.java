package com.habsida.store.repository;

import com.habsida.store.entity.Order;
import com.habsida.store.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    /**
     * Orders that have at least one order item whose product belongs to one of the given stores,
     * or orders with store_id in the given list.
     */
    @Query("SELECT o FROM Order o WHERE o.id IN (SELECT oi.orderId FROM OrderItem oi JOIN oi.product p WHERE p.storeId IN :storeIds)")
    Page<Order> findByStoreIds(@Param("storeIds") List<Long> storeIds, Pageable pageable);

    /**
     * Same as findByStoreIds but also by order.storeId, with optional status filter.
     */
    @Query("""
            SELECT o FROM Order o
            WHERE (o.storeId IN :storeIds OR o.id IN (
                SELECT oi.orderId FROM OrderItem oi JOIN oi.product p WHERE p.storeId IN :storeIds
            ))
            AND (
                :status IS NULL
                OR o.status = :status
                OR (:status = com.habsida.store.enums.OrderStatus.ACCEPTED  AND o.status = com.habsida.store.enums.OrderStatus.CONFIRMED)
                OR (:status = com.habsida.store.enums.OrderStatus.IN_PROGRESS AND (o.status = com.habsida.store.enums.OrderStatus.PROCESSING OR o.status = com.habsida.store.enums.OrderStatus.READY OR o.status = com.habsida.store.enums.OrderStatus.SHIPPED))
                OR (:status = com.habsida.store.enums.OrderStatus.COMPLETED  AND o.status = com.habsida.store.enums.OrderStatus.DELIVERED)
                OR (:status = com.habsida.store.enums.OrderStatus.CANCELED   AND o.status = com.habsida.store.enums.OrderStatus.CANCELLED)
                OR (:status = com.habsida.store.enums.OrderStatus.NEW        AND o.status = com.habsida.store.enums.OrderStatus.PENDING)
            )
            """)
    Page<Order> findByStoreIdsAndStatus(@Param("storeIds") List<Long> storeIds, @Param("status") OrderStatus status, Pageable pageable);

    @Query("SELECT COUNT(oi) FROM OrderItem oi JOIN oi.product p WHERE oi.orderId = :orderId AND p.storeId IN :storeIds")
    long countOrderItemsInStores(@Param("orderId") Long orderId, @Param("storeIds") List<Long> storeIds);
}
