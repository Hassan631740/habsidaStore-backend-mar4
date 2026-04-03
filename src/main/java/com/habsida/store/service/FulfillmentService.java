package com.habsida.store.service;

import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.FulfillmentRequest;
import com.habsida.store.dto.response.FulfillmentResponse;
import com.habsida.store.entity.Fulfillment;
import com.habsida.store.repository.FulfillmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FulfillmentService {

    private final FulfillmentRepository repository;

    @Transactional(readOnly = true)
    public PageResponse<FulfillmentResponse> findAll(Pageable pageable) {
        return PageResponse.of(repository.findAll(pageable).map(DtoMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public Optional<FulfillmentResponse> findById(Long id) {
        return repository.findById(id).map(DtoMapper::toResponse);
    }

    @Transactional
    public FulfillmentResponse create(FulfillmentRequest request) {
        Fulfillment entity = DtoMapper.toEntity(request);
        return DtoMapper.toResponse(repository.save(entity));
    }

    @Transactional
    public Optional<FulfillmentResponse> update(Long id, FulfillmentRequest request) {
        if (!repository.existsById(id)) {
            return Optional.empty();
        }
        Fulfillment entity = DtoMapper.toEntity(request);
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