package com.habsida.store.repository;

import com.habsida.store.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Page<Category> findByStoreId(Long storeId, Pageable pageable);

    Page<Category> findByStoreIdIn(List<Long> storeIds, Pageable pageable);
}
