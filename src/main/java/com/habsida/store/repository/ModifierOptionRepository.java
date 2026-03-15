package com.habsida.store.repository;

import com.habsida.store.entity.ModifierOption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModifierOptionRepository extends JpaRepository<ModifierOption, Long> {

    Page<ModifierOption> findByModifierGroupId(Long modifierGroupId, Pageable pageable);

    List<ModifierOption> findByModifierGroupIdIn(List<Long> modifierGroupIds);
}
