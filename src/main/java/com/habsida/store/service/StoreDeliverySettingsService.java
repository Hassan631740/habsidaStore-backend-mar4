package com.habsida.store.service;

import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.StoreDeliverySettingsRequest;
import com.habsida.store.dto.response.StoreDeliverySettingsResponse;
import com.habsida.store.entity.StoreDeliverySettings;
import com.habsida.store.repository.StoreDeliverySettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StoreDeliverySettingsService {

    private final StoreDeliverySettingsRepository repository;

    @Transactional(readOnly = true)
    public PageResponse<StoreDeliverySettingsResponse> findAll(Pageable pageable) {
        return PageResponse.of(repository.findAll(pageable).map(DtoMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public Optional<StoreDeliverySettingsResponse> findById(Long id) {
        return repository.findById(id).map(DtoMapper::toResponse);
    }

    @Transactional
    public StoreDeliverySettingsResponse create(StoreDeliverySettingsRequest request) {
        StoreDeliverySettings entity = DtoMapper.toEntity(request);
        return DtoMapper.toResponse(repository.save(entity));
    }

    @Transactional
    public Optional<StoreDeliverySettingsResponse> update(Long id, StoreDeliverySettingsRequest request) {
        if (!repository.existsById(id)) {
            return Optional.empty();
        }
        StoreDeliverySettings entity = DtoMapper.toEntity(request);
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