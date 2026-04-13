package com.habsida.store.service;

import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.StoreBreaksRequest;
import com.habsida.store.dto.response.StoreBreaksResponse;
import com.habsida.store.entity.StoreBreaks;
import com.habsida.store.repository.StoreBreaksRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StoreBreaksService {

    private final StoreBreaksRepository repository;

    @Transactional(readOnly = true)
    public PageResponse<StoreBreaksResponse> findAll(Pageable pageable) {
        return PageResponse.of(repository.findAll(pageable).map(DtoMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public Optional<StoreBreaksResponse> findById(Long id) {
        return repository.findById(id).map(DtoMapper::toResponse);
    }

    @Transactional
    public StoreBreaksResponse create(StoreBreaksRequest request) {
        StoreBreaks entity = DtoMapper.toEntity(request);
        return DtoMapper.toResponse(repository.save(entity));
    }

    @Transactional
    public Optional<StoreBreaksResponse> update(Long id, StoreBreaksRequest request) {
        if (!repository.existsById(id)) {
            return Optional.empty();
        }
        StoreBreaks entity = DtoMapper.toEntity(request);
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