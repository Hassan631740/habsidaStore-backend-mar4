package com.habsida.store.service;

import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.AddressRequest;
import com.habsida.store.dto.response.AddressResponse;
import com.habsida.store.entity.Address;
import com.habsida.store.exception.ResourceNotFoundException;
import com.habsida.store.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository repository;

    @Transactional(readOnly = true)
    public PageResponse<AddressResponse> findAll(Pageable pageable) {
        return PageResponse.of(repository.findAll(pageable).map(DtoMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public Optional<AddressResponse> findById(Long id) {
        return repository.findById(id).map(DtoMapper::toResponse);
    }

    @Transactional
    public AddressResponse create(AddressRequest request) {
        Address entity = DtoMapper.toEntity(request);
        return DtoMapper.toResponse(repository.save(entity));
    }

    @Transactional
    public Optional<AddressResponse> update(Long id, AddressRequest request) {
        if (!repository.existsById(id)) {
            return Optional.empty();
        }
        Address entity = DtoMapper.toEntity(request);
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

    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    public Address getReferenceById(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Address", id);
        }
        return repository.getReferenceById(id);
    }
}