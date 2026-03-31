package com.habsida.store.repository;

import com.habsida.store.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    long countByOrderId(Long orderId);
}
