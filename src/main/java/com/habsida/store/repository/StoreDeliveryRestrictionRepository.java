package com.habsida.store.repository;

import com.habsida.store.entity.StoreDeliveryRestriction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreDeliveryRestrictionRepository extends JpaRepository<StoreDeliveryRestriction, Long> {

    List<StoreDeliveryRestriction> findByStoreId(Long storeId);

    void deleteByStoreId(Long storeId);
}