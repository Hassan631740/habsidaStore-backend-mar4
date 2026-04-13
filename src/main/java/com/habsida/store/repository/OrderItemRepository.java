package com.habsida.store.repository;

import com.habsida.store.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    long countByOrderId(Long orderId);

    List<OrderItem> findByOrderId(Long orderId);
}
