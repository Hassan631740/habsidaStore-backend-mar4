package com.habsida.store.service;

import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.AssignMerchantRequest;
import com.habsida.store.dto.request.UserStoreAccessRequest;
import com.habsida.store.dto.response.UserStoreAccessResponse;
import com.habsida.store.entity.UserStoreAccess;
import com.habsida.store.exception.ResourceNotFoundException;
import com.habsida.store.repository.StoreRepository;
import com.habsida.store.repository.UserRepository;
import com.habsida.store.repository.UserStoreAccessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserStoreAccessService {

    private final UserStoreAccessRepository repository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    // --- Basic CRUD (used by UserStoreAccessController) ---

    @Transactional(readOnly = true)
    public PageResponse<UserStoreAccessResponse> findAll(Pageable pageable) {
        return PageResponse.of(repository.findAll(pageable).map(DtoMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public Optional<UserStoreAccessResponse> findById(Long id) {
        return repository.findById(id).map(DtoMapper::toResponse);
    }

    @Transactional
    public UserStoreAccessResponse create(UserStoreAccessRequest request) {
        UserStoreAccess entity = DtoMapper.toEntity(request);
        return DtoMapper.toResponse(repository.save(entity));
    }

    @Transactional
    public Optional<UserStoreAccessResponse> update(Long id, UserStoreAccessRequest request) {
        if (!repository.existsById(id)) {
            return Optional.empty();
        }
        UserStoreAccess entity = DtoMapper.toEntity(request);
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

    // --- Admin store-merchant assignment operations ---

    @Transactional(readOnly = true)
    public List<UserStoreAccessResponse> listByStore(Long storeId) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", storeId);
        }
        return repository.findByStoreId(storeId).stream()
                .map(DtoMapper::toResponse)
                .toList();
    }

    @Transactional
    public UserStoreAccessResponse assignMerchant(Long storeId, AssignMerchantRequest request) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", storeId);
        }
        if (!userRepository.existsById(request.getUserId())) {
            throw new ResourceNotFoundException("User", request.getUserId());
        }
        if (repository.existsByUserIdAndStoreId(request.getUserId(), storeId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already assigned to this store");
        }
        UserStoreAccess entity = UserStoreAccess.builder()
                .userId(request.getUserId())
                .storeId(storeId)
                .build();
        return DtoMapper.toResponse(repository.save(entity));
    }

    @Transactional
    public void unassignMerchant(Long storeId, Long userId) {
        List<UserStoreAccess> list = repository.findByStoreId(storeId).stream()
                .filter(usa -> userId.equals(usa.getUserId()))
                .collect(Collectors.toList());
        if (list.isEmpty()) {
            throw new ResourceNotFoundException("User not assigned to this store");
        }
        list.forEach(repository::delete);
    }

    /** Returns the store IDs accessible by a given user. */
    @Transactional(readOnly = true)
    public List<Long> getStoreIdsByUser(Long userId) {
        return repository.findByUserId(userId).stream()
                .map(UserStoreAccess::getStoreId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
}