package com.habsida.store.service;

import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.CartItemRequest;
import com.habsida.store.dto.response.CartItemResponse;
import com.habsida.store.entity.CartItem;
import com.habsida.store.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartItemService {

    private final CartItemRepository repository;

    @Transactional(readOnly = true)
    public PageResponse<CartItemResponse> findAll(Pageable pageable) {
        return PageResponse.of(repository.findAll(pageable).map(DtoMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public Optional<CartItemResponse> findById(Long id) {
        return repository.findById(id).map(DtoMapper::toResponse);
    }

    @Transactional
    public CartItemResponse create(CartItemRequest request) {
        CartItem entity = DtoMapper.toEntity(request);
        return DtoMapper.toResponse(repository.save(entity));
    }

    @Transactional
    public Optional<CartItemResponse> update(Long id, CartItemRequest request) {
        if (!repository.existsById(id)) {
            return Optional.empty();
        }
        CartItem entity = DtoMapper.toEntity(request);
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