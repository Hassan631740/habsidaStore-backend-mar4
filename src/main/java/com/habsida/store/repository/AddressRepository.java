package com.habsida.store.repository;

import com.habsida.store.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * {@link JpaRepository#findAllById(Iterable)} loads many addresses in one query ({@code WHERE id IN (...)}).
 */
public interface AddressRepository extends JpaRepository<Address, Long> {
}
