package com.habsida.store.repository;

import com.habsida.store.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Long> {

    Page<Store> findByIdIn(List<Long> ids, Pageable pageable);
}
