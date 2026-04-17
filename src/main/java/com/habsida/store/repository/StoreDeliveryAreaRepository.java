package com.habsida.store.repository;

import com.habsida.store.entity.StoreDeliveryArea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreDeliveryAreaRepository extends JpaRepository<StoreDeliveryArea, Long> {

    List<StoreDeliveryArea> findByStoreId(Long storeId);

    void deleteByStoreId(Long storeId);
}