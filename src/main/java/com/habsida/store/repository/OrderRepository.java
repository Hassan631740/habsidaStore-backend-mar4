package com.habsida.store.repository;

import com.habsida.store.entity.Order;
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
    @Query("SELECT o FROM Order o WHERE (o.storeId IN :storeIds OR o.id IN (SELECT oi.orderId FROM OrderItem oi JOIN oi.product p WHERE p.storeId IN :storeIds)) AND (:status IS NULL OR :status = '' OR o.status = :status)")
    Page<Order> findByStoreIdsAndStatus(@Param("storeIds") List<Long> storeIds, @Param("status") String status, Pageable pageable);

    @Query("SELECT COUNT(oi) FROM OrderItem oi JOIN oi.product p WHERE oi.orderId = :orderId AND p.storeId IN :storeIds")
    long countOrderItemsInStores(@Param("orderId") Long orderId, @Param("storeIds") List<Long> storeIds);

    /**
     * True if the order belongs to a merchant: either order.storeId is in the list,
     * or at least one order item's product belongs to one of the stores.
     */
    @Query("SELECT COUNT(o) > 0 FROM Order o WHERE o.id = :orderId AND (o.storeId IN :storeIds OR o.id IN (SELECT oi.orderId FROM OrderItem oi JOIN oi.product p WHERE p.storeId IN :storeIds))")
    boolean existsByIdAndStoreIds(@Param("orderId") Long orderId, @Param("storeIds") List<Long> storeIds);
}
