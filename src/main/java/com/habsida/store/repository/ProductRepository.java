package com.habsida.store.repository;

import com.habsida.store.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Page<Product> findByStoreId(Long storeId, Pageable pageable);

    Page<Product> findByStoreIdIn(List<Long> storeIds, Pageable pageable);
}
