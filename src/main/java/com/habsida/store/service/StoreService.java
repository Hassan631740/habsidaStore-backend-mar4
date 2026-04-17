package com.habsida.store.service;

import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.StoreRequest;
import com.habsida.store.dto.response.StoreResponse;
import com.habsida.store.dto.response.UserStoreAccessResponse;
import com.habsida.store.entity.Store;
import com.habsida.store.entity.UserStoreAccess;
import com.habsida.store.enums.StoreStatus;
import com.habsida.store.exception.ResourceNotFoundException;
import com.habsida.store.repository.AddressRepository;
import com.habsida.store.repository.StoreRepository;
import com.habsida.store.repository.UserStoreAccessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final AddressRepository addressRepository;
    private final MerchantStoreAccessService merchantStoreAccessService;
    private final UserStoreAccessRepository userStoreAccessRepository;

    @Transactional(readOnly = true)
    public PageResponse<StoreResponse> findAll(Pageable pageable) {
        return PageResponse.of(storeRepository.findAll(pageable).map(DtoMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public PageResponse<StoreResponse> findAll(String status, String location, Pageable pageable) {
        return PageResponse.of(storeRepository.findByFilters(status, location, pageable).map(DtoMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public Optional<StoreResponse> findById(Long id) {
        return storeRepository.findById(id).map(DtoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public StoreResponse getById(Long id) {
        return DtoMapper.toResponse(
                storeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Store", id)));
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return storeRepository.existsById(id);
    }

    /** Returns all stores the given user has merchant access to. */
    @Transactional(readOnly = true)
    public PageResponse<StoreResponse> findAllForMerchant(Long userId, Pageable pageable) {
        List<Long> storeIds = merchantStoreAccessService.getStoreIds(userId);
        if (storeIds.isEmpty()) {
            return PageResponse.of(Page.empty(pageable));
        }
        return PageResponse.of(storeRepository.findByIdIn(storeIds, pageable).map(DtoMapper::toResponse));
    }

    /** Returns a specific store only if the user has merchant access to it. */
    @Transactional(readOnly = true)
    public StoreResponse getByIdForMerchant(Long userId, Long id) {
        List<Long> storeIds = merchantStoreAccessService.getStoreIds(userId);
        if (!storeIds.contains(id)) {
            throw new ResourceNotFoundException("Store", id);
        }
        return DtoMapper.toResponse(
                storeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Store", id)));
    }

    @Transactional
    public StoreResponse create(StoreRequest request) {
        Store entity = DtoMapper.toEntity(request);
        entity.setAddress(addressRepository.getReferenceById(request.getAddressId()));
        return DtoMapper.toResponse(storeRepository.save(entity));
    }

    @Transactional
    public StoreResponse update(Long id, StoreRequest request) {
        if (!storeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Store", id);
        }
        Store entity = DtoMapper.toEntity(request);
        entity.setId(id);
        entity.setAddress(addressRepository.getReferenceById(request.getAddressId()));
        return DtoMapper.toResponse(storeRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        if (!storeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Store", id);
        }
        storeRepository.deleteById(id);
    }

    @Transactional
    public StoreResponse updateStatus(Long id, StoreStatus status) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store", id));
        store.setStatus(status.name());
        return DtoMapper.toResponse(storeRepository.save(store));
    }

    @Transactional
    public UserStoreAccessResponse assignUserAccess(Long storeId, Long userId) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", storeId);
        }
        if (userStoreAccessRepository.existsByUserIdAndStoreId(userId, storeId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already has access to this store");
        }
        UserStoreAccess access = new UserStoreAccess();
        access.setUserId(userId);
        access.setStoreId(storeId);
        return DtoMapper.toResponse(userStoreAccessRepository.save(access));
    }

}