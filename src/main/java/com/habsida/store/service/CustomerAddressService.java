package com.habsida.store.service;

import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.CustomerAddressRequest;
import com.habsida.store.dto.response.CustomerAddressResponse;
import com.habsida.store.entity.CustomerAddress;
import com.habsida.store.repository.CustomerAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerAddressService {

    private final CustomerAddressRepository repository;

    @Transactional(readOnly = true)
    public PageResponse<CustomerAddressResponse> findAll(Pageable pageable) {
        return PageResponse.of(repository.findAll(pageable).map(DtoMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public Optional<CustomerAddressResponse> findById(Long id) {
        return repository.findById(id).map(DtoMapper::toResponse);
    }

    @Transactional
    public CustomerAddressResponse create(CustomerAddressRequest request) {
        CustomerAddress entity = DtoMapper.toEntity(request);
        return DtoMapper.toResponse(repository.save(entity));
    }

    @Transactional
    public Optional<CustomerAddressResponse> update(Long id, CustomerAddressRequest request) {
        if (!repository.existsById(id)) {
            return Optional.empty();
        }
        CustomerAddress entity = DtoMapper.toEntity(request);
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