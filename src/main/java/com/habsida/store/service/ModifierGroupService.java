package com.habsida.store.service;

import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.ModifierGroupRequest;
import com.habsida.store.dto.response.ModifierGroupResponse;
import com.habsida.store.entity.ModifierGroup;
import com.habsida.store.entity.UserStoreAccess;
import com.habsida.store.exception.ResourceNotFoundException;
import com.habsida.store.repository.ModifierGroupRepository;
import com.habsida.store.repository.StoreRepository;
import com.habsida.store.repository.UserStoreAccessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ModifierGroupService {

    private final ModifierGroupRepository repository;
    private final StoreRepository storeRepository;
    private final UserStoreAccessRepository userStoreAccessRepository;

    // --- Basic CRUD (used by ModifierGroupController) ---

    @Transactional(readOnly = true)
    public PageResponse<ModifierGroupResponse> findAll(Pageable pageable) {
        return PageResponse.of(repository.findAll(pageable).map(DtoMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public Optional<ModifierGroupResponse> findById(Long id) {
        return repository.findById(id).map(DtoMapper::toResponse);
    }

    @Transactional
    public ModifierGroupResponse create(ModifierGroupRequest request) {
        ModifierGroup entity = DtoMapper.toEntity(request);
        return DtoMapper.toResponse(repository.save(entity));
    }

    @Transactional
    public Optional<ModifierGroupResponse> update(Long id, ModifierGroupRequest request) {
        if (!repository.existsById(id)) {
            return Optional.empty();
        }
        ModifierGroup entity = DtoMapper.toEntity(request);
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

    // --- Admin (store-scoped) operations ---

    @Transactional(readOnly = true)
    public PageResponse<ModifierGroupResponse> findByStoreId(Long storeId, Pageable pageable) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", storeId);
        }
        return PageResponse.of(repository.findByStoreId(storeId, pageable).map(DtoMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public ModifierGroupResponse getByIdForStore(Long storeId, Long id) {
        ModifierGroup g = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ModifierGroup", id));
        if (!storeId.equals(g.getStoreId())) {
            throw new ResourceNotFoundException("ModifierGroup", id);
        }
        return DtoMapper.toResponse(g);
    }

    @Transactional
    public ModifierGroupResponse createForStore(Long storeId, ModifierGroupRequest request) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", storeId);
        }
        if (request.getStoreId() != null && !request.getStoreId().equals(storeId)) {
            throw new IllegalArgumentException("Store ID must match path");
        }
        ModifierGroup entity = DtoMapper.toEntity(request);
        entity.setStoreId(storeId);
        return DtoMapper.toResponse(repository.save(entity));
    }

    @Transactional
    public ModifierGroupResponse updateForStore(Long storeId, Long id, ModifierGroupRequest request) {
        ModifierGroup existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ModifierGroup", id));
        if (!storeId.equals(existing.getStoreId())) {
            throw new ResourceNotFoundException("ModifierGroup", id);
        }
        if (request.getStoreId() != null && !request.getStoreId().equals(storeId)) {
            throw new IllegalArgumentException("Store ID must match path");
        }
        ModifierGroup entity = DtoMapper.toEntity(request);
        entity.setId(id);
        entity.setStoreId(storeId);
        return DtoMapper.toResponse(repository.save(entity));
    }

    @Transactional
    public void deleteForStore(Long storeId, Long id) {
        ModifierGroup existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ModifierGroup", id));
        if (!storeId.equals(existing.getStoreId())) {
            throw new ResourceNotFoundException("ModifierGroup", id);
        }
        repository.deleteById(id);
    }

    // --- Merchant (user-scoped) operations ---

    @Transactional(readOnly = true)
    public PageResponse<ModifierGroupResponse> findAllForMerchant(Long userId, Pageable pageable) {
        List<Long> storeIds = getMerchantStoreIds(userId);
        if (storeIds.isEmpty()) {
            return PageResponse.of(Page.empty(pageable));
        }
        return PageResponse.of(repository.findByStoreIdIn(storeIds, pageable).map(DtoMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public ModifierGroupResponse getByIdForMerchant(Long userId, Long id) {
        List<Long> storeIds = getMerchantStoreIds(userId);
        ModifierGroup g = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ModifierGroup", id));
        if (!storeIds.contains(g.getStoreId())) {
            throw new ResourceNotFoundException("ModifierGroup", id);
        }
        return DtoMapper.toResponse(g);
    }

    @Transactional
    public ModifierGroupResponse createForMerchant(Long userId, ModifierGroupRequest request) {
        List<Long> storeIds = getMerchantStoreIds(userId);
        if (request.getStoreId() == null || !storeIds.contains(request.getStoreId())) {
            throw new ResourceNotFoundException("Store", request.getStoreId());
        }
        ModifierGroup entity = DtoMapper.toEntity(request);
        return DtoMapper.toResponse(repository.save(entity));
    }

    @Transactional
    public ModifierGroupResponse updateForMerchant(Long userId, Long id, ModifierGroupRequest request) {
        List<Long> storeIds = getMerchantStoreIds(userId);
        ModifierGroup existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ModifierGroup", id));
        if (!storeIds.contains(existing.getStoreId())) {
            throw new ResourceNotFoundException("ModifierGroup", id);
        }
        if (request.getStoreId() != null && !storeIds.contains(request.getStoreId())) {
            throw new ResourceNotFoundException("Store", request.getStoreId());
        }
        ModifierGroup entity = DtoMapper.toEntity(request);
        entity.setId(id);
        return DtoMapper.toResponse(repository.save(entity));
    }

    @Transactional
    public void deleteForMerchant(Long userId, Long id) {
        List<Long> storeIds = getMerchantStoreIds(userId);
        ModifierGroup existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ModifierGroup", id));
        if (!storeIds.contains(existing.getStoreId())) {
            throw new ResourceNotFoundException("ModifierGroup", id);
        }
        repository.deleteById(id);
    }

    /** Verifies a group belongs to a given store (used by ModifierOptionService). */
    @Transactional(readOnly = true)
    public ModifierGroup requireGroupForStore(Long storeId, Long groupId) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", storeId);
        }
        ModifierGroup g = repository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("ModifierGroup", groupId));
        if (!storeId.equals(g.getStoreId())) {
            throw new ResourceNotFoundException("ModifierGroup", groupId);
        }
        return g;
    }

    /** Verifies a group belongs to one of the merchant's stores (used by ModifierOptionService). */
    @Transactional(readOnly = true)
    public ModifierGroup requireGroupForMerchant(Long userId, Long groupId) {
        List<Long> storeIds = getMerchantStoreIds(userId);
        ModifierGroup g = repository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("ModifierGroup", groupId));
        if (!storeIds.contains(g.getStoreId())) {
            throw new ResourceNotFoundException("ModifierGroup", groupId);
        }
        return g;
    }

    private List<Long> getMerchantStoreIds(Long userId) {
        return userStoreAccessRepository.findByUserId(userId).stream()
                .map(UserStoreAccess::getStoreId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
}