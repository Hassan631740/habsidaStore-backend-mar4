package com.habsida.store.repository;

import com.habsida.store.entity.OrderItemModifier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemModifierRepository extends JpaRepository<OrderItemModifier, Long> {
}
