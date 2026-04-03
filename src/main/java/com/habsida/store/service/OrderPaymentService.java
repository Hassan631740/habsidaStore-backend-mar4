package com.habsida.store.service;

import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.OrderPaymentRequest;
import com.habsida.store.dto.response.OrderPaymentResponse;
import com.habsida.store.entity.OrderPayment;
import com.habsida.store.repository.OrderPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderPaymentService {

    private final OrderPaymentRepository repository;

    @Transactional(readOnly = true)
    public PageResponse<OrderPaymentResponse> findAll(Pageable pageable) {
        return PageResponse.of(repository.findAll(pageable).map(DtoMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public Optional<OrderPaymentResponse> findById(Long id) {
        return repository.findById(id).map(DtoMapper::toResponse);
    }

    @Transactional
    public OrderPaymentResponse create(OrderPaymentRequest request) {
        OrderPayment entity = DtoMapper.toEntity(request);
        return DtoMapper.toResponse(repository.save(entity));
    }

    @Transactional
    public Optional<OrderPaymentResponse> update(Long id, OrderPaymentRequest request) {
        if (!repository.existsById(id)) {
            return Optional.empty();
        }
        OrderPayment entity = DtoMapper.toEntity(request);
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