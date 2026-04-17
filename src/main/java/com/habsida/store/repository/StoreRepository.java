package com.habsida.store.repository;

import com.habsida.store.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Long> {

    Page<Store> findByIdIn(List<Long> ids, Pageable pageable);

    @Query("SELECT s FROM Store s WHERE " +
           "(:status IS NULL OR s.status = :status) AND " +
           "(:location IS NULL OR LOWER(s.location) LIKE LOWER(CONCAT('%', :location, '%')))")
    Page<Store> findByFilters(@Param("status") String status,
                              @Param("location") String location,
                              Pageable pageable);
}