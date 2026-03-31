package com.habsida.store.repository;

import com.habsida.store.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

    @Query("""
            SELECT DISTINCT c FROM Customer c
            JOIN Order o ON o.customerId = c.id
            JOIN OrderItem oi ON oi.orderId = o.id
            JOIN Product p ON p.id = oi.productId
            WHERE p.storeId IN :storeIds
            """)
    Page<Customer> findDistinctCustomersWhoOrderedFromStores(@Param("storeIds") List<Long> storeIds, Pageable pageable);

    @Query("""
            SELECT DISTINCT c FROM Customer c
            JOIN Order o ON o.customerId = c.id
            JOIN OrderItem oi ON oi.orderId = o.id
            JOIN Product p ON p.id = oi.productId
            WHERE p.storeId = :storeId
            """)
    Page<Customer> findDistinctCustomersWhoOrderedFromStore(@Param("storeId") Long storeId, Pageable pageable);
}
