package com.habsida.store.repository;

import com.habsida.store.entity.OrderAddress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderAddressRepository extends JpaRepository<OrderAddress, Long> {
}
