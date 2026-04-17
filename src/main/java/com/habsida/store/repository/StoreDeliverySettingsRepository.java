package com.habsida.store.repository;

import com.habsida.store.entity.StoreDeliverySettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreDeliverySettingsRepository extends JpaRepository<StoreDeliverySettings, Long> {

    Optional<StoreDeliverySettings> findByStoreId(Long storeId);
}