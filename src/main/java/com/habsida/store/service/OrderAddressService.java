package com.habsida.store.service;

import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.OrderAddressRequest;
import com.habsida.store.dto.response.OrderAddressResponse;
import com.habsida.store.entity.OrderAddress;
import com.habsida.store.repository.OrderAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderAddressService {

    private final OrderAddressRepository repository;

    @Transactional(readOnly = true)
    public PageResponse<OrderAddressResponse> findAll(Pageable pageable) {
        return PageResponse.of(repository.findAll(pageable).map(DtoMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public Optional<OrderAddressResponse> findById(Long id) {
        return repository.findById(id).map(DtoMapper::toResponse);
    }

    @Transactional
    public OrderAddressResponse create(OrderAddressRequest request) {
        OrderAddress entity = DtoMapper.toEntity(request);
        return DtoMapper.toResponse(repository.save(entity));
    }

    @Transactional
    public Optional<OrderAddressResponse> update(Long id, OrderAddressRequest request) {
        if (!repository.existsById(id)) {
            return Optional.empty();
        }
        OrderAddress entity = DtoMapper.toEntity(request);
        entity.setId(id);
        return Optional.of(DtoMapper.toResponse(repository.save(entity)));
    }

    @Transactional
    public boolean delete(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }
}