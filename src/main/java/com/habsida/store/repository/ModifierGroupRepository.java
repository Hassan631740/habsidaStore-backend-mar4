package com.habsida.store.repository;

import com.habsida.store.entity.ModifierGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModifierGroupRepository extends JpaRepository<ModifierGroup, Long> {

    Page<ModifierGroup> findByStoreId(Long storeId, Pageable pageable);

    Page<ModifierGroup> findByStoreIdIn(List<Long> storeIds, Pageable pageable);
}
