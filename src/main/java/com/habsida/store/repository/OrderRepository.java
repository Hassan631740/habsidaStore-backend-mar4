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
     * Orders that have at least one order item whose product belongs to one of the given stores.
     */
    @Query("SELECT o FROM Order o WHERE o.id IN (SELECT oi.orderId FROM OrderItem oi JOIN oi.product p WHERE p.storeId IN :storeIds)")
    Page<Order> findByStoreIds(@Param("storeIds") List<Long> storeIds, Pageable pageable);

    @Query("SELECT COUNT(oi) FROM OrderItem oi JOIN oi.product p WHERE oi.orderId = :orderId AND p.storeId IN :storeIds")
    long countOrderItemsInStores(@Param("orderId") Long orderId, @Param("storeIds") List<Long> storeIds);
}
