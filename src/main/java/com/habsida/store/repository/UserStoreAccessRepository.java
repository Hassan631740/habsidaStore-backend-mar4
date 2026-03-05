package com.habsida.store.repository;

import com.habsida.store.entity.UserStoreAccess;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserStoreAccessRepository extends JpaRepository<UserStoreAccess, Long> {
}
