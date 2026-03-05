package com.habsida.store.repository;

import com.habsida.store.entity.Fulfillment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FulfillmentRepository extends JpaRepository<Fulfillment, Long> {
}
