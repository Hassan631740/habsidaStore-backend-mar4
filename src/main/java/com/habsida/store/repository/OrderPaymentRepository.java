package com.habsida.store.repository;

import com.habsida.store.entity.OrderPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderPaymentRepository extends JpaRepository<OrderPayment, Long> {

    Optional<OrderPayment> findByOrderId(Long orderId);
}