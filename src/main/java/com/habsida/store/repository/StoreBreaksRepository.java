package com.habsida.store.repository;

import com.habsida.store.entity.StoreBreaks;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreBreaksRepository extends JpaRepository<StoreBreaks, Long> {

    List<StoreBreaks> findByStoreId(Long storeId);

    void deleteByStoreId(Long storeId);
}