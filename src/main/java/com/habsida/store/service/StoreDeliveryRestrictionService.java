package com.habsida.store.service;

import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.StoreDeliveryRestrictionRequest;
import com.habsida.store.dto.response.StoreDeliveryRestrictionResponse;
import com.habsida.store.entity.StoreDeliveryRestriction;
import com.habsida.store.repository.StoreDeliveryRestrictionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StoreDeliveryRestrictionService {

    private final StoreDeliveryRestrictionRepository repository;

    @Transactional(readOnly = true)
    public PageResponse<StoreDeliveryRestrictionResponse> findAll(Pageable pageable) {
        return PageResponse.of(repository.findAll(pageable).map(DtoMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public Optional<StoreDeliveryRestrictionResponse> findById(Long id) {
        return repository.findById(id).map(DtoMapper::toResponse);
    }

    @Transactional
    public StoreDeliveryRestrictionResponse create(StoreDeliveryRestrictionRequest request) {
        StoreDeliveryRestriction entity = DtoMapper.toEntity(request);
        return DtoMapper.toResponse(repository.save(entity));
    }

    @Transactional
    public Optional<StoreDeliveryRestrictionResponse> update(Long id, StoreDeliveryRestrictionRequest request) {
        if (!repository.existsById(id)) {
            return Optional.empty();
        }
        StoreDeliveryRestriction entity = DtoMapper.toEntity(request);
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