package com.habsida.store.repository;

import com.habsida.store.entity.UserStoreAccess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserStoreAccessRepository extends JpaRepository<UserStoreAccess, Long> {

    List<UserStoreAccess> findByUserId(Long userId);
}
