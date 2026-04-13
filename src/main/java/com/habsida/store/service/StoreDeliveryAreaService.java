package com.habsida.store.service;

import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.StoreDeliveryAreaRequest;
import com.habsida.store.dto.response.StoreDeliveryAreaResponse;
import com.habsida.store.entity.StoreDeliveryArea;
import com.habsida.store.repository.StoreDeliveryAreaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StoreDeliveryAreaService {

    private final StoreDeliveryAreaRepository repository;

    @Transactional(readOnly = true)
    public PageResponse<StoreDeliveryAreaResponse> findAll(Pageable pageable) {
        return PageResponse.of(repository.findAll(pageable).map(DtoMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public Optional<StoreDeliveryAreaResponse> findById(Long id) {
        return repository.findById(id).map(DtoMapper::toResponse);
    }

    @Transactional
    public StoreDeliveryAreaResponse create(StoreDeliveryAreaRequest request) {
        StoreDeliveryArea entity = DtoMapper.toEntity(request);
        return DtoMapper.toResponse(repository.save(entity));
    }

    @Transactional
    public Optional<StoreDeliveryAreaResponse> update(Long id, StoreDeliveryAreaRequest request) {
        if (!repository.existsById(id)) {
            return Optional.empty();
        }
        StoreDeliveryArea entity = DtoMapper.toEntity(request);
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