package com.habsida.store.service;

import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.StoreHoursRequest;
import com.habsida.store.dto.response.StoreHoursResponse;
import com.habsida.store.entity.StoreHours;
import com.habsida.store.repository.StoreHoursRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StoreHoursService {

    private final StoreHoursRepository repository;

    @Transactional(readOnly = true)
    public PageResponse<StoreHoursResponse> findAll(Pageable pageable) {
        return PageResponse.of(repository.findAll(pageable).map(DtoMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public Optional<StoreHoursResponse> findById(Long id) {
        return repository.findById(id).map(DtoMapper::toResponse);
    }

    @Transactional
    public StoreHoursResponse create(StoreHoursRequest request) {
        StoreHours entity = DtoMapper.toEntity(request);
        return DtoMapper.toResponse(repository.save(entity));
    }

    @Transactional
    public Optional<StoreHoursResponse> update(Long id, StoreHoursRequest request) {
        if (!repository.existsById(id)) {
            return Optional.empty();
        }
        StoreHours entity = DtoMapper.toEntity(request);
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