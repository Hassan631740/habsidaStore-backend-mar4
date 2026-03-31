package com.habsida.store.repository;

import com.habsida.store.entity.ProductModifierGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductModifierGroupRepository extends JpaRepository<ProductModifierGroup, Long> {

    List<ProductModifierGroup> findByProductId(Long productId);

    Page<ProductModifierGroup> findByProductId(Long productId, Pageable pageable);

    boolean existsByProductIdAndModifierGroupId(Long productId, Long modifierGroupId);
}
