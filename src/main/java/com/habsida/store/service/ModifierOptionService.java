package com.habsida.store.service;

import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.ModifierOptionRequest;
import com.habsida.store.dto.response.ModifierOptionResponse;
import com.habsida.store.entity.ModifierOption;
import com.habsida.store.exception.ResourceNotFoundException;
import com.habsida.store.repository.ModifierOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ModifierOptionService {

    private final ModifierOptionRepository repository;
    private final ModifierGroupService modifierGroupService;

    // --- Basic CRUD (used by ModifierOptionController) ---

    @Transactional(readOnly = true)
    public PageResponse<ModifierOptionResponse> findAll(Pageable pageable) {
        return PageResponse.of(repository.findAll(pageable).map(DtoMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public Optional<ModifierOptionResponse> findById(Long id) {
        return repository.findById(id).map(DtoMapper::toResponse);
    }

    @Transactional
    public ModifierOptionResponse create(ModifierOptionRequest request) {
        ModifierOption entity = DtoMapper.toEntity(request);
        return DtoMapper.toResponse(repository.save(entity));
    }

    @Transactional
    public Optional<ModifierOptionResponse> update(Long id, ModifierOptionRequest request) {
        if (!repository.existsById(id)) {
            return Optional.empty();
        }
        ModifierOption entity = DtoMapper.toEntity(request);
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

    // --- Admin (store + group-scoped) operations ---

    @Transactional(readOnly = true)
    public PageResponse<ModifierOptionResponse> findByGroupForStore(Long storeId, Long groupId, Pageable pageable) {
        modifierGroupService.requireGroupForStore(storeId, groupId);
        return PageResponse.of(repository.findByModifierGroupId(groupId, pageable).map(DtoMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public ModifierOptionResponse getByIdForStore(Long storeId, Long groupId, Long id) {
        modifierGroupService.requireGroupForStore(storeId, groupId);
        ModifierOption opt = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ModifierOption", id));
        if (!groupId.equals(opt.getModifierGroupId())) {
            throw new ResourceNotFoundException("ModifierOption", id);
        }
        return DtoMapper.toResponse(opt);
    }

    @Transactional
    public ModifierOptionResponse createForStore(Long storeId, Long groupId, ModifierOptionRequest request) {
        modifierGroupService.requireGroupForStore(storeId, groupId);
        if (request.getModifierGroupId() != null && !request.getModifierGroupId().equals(groupId)) {
            throw new IllegalArgumentException("Modifier group ID must match path");
        }
        ModifierOption entity = DtoMapper.toEntity(request);
        entity.setModifierGroupId(groupId);
        return DtoMapper.toResponse(repository.save(entity));
    }

    @Transactional
    public ModifierOptionResponse updateForStore(Long storeId, Long groupId, Long id, ModifierOptionRequest request) {
        modifierGroupService.requireGroupForStore(storeId, groupId);
        ModifierOption existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ModifierOption", id));
        if (!groupId.equals(existing.getModifierGroupId())) {
            throw new ResourceNotFoundException("ModifierOption", id);
        }
        if (request.getModifierGroupId() != null && !request.getModifierGroupId().equals(groupId)) {
            throw new IllegalArgumentException("Modifier group ID must match path");
        }
        ModifierOption entity = DtoMapper.toEntity(request);
        entity.setId(id);
        entity.setModifierGroupId(groupId);
        return DtoMapper.toResponse(repository.save(entity));
    }

    @Transactional
    public void deleteForStore(Long storeId, Long groupId, Long id) {
        modifierGroupService.requireGroupForStore(storeId, groupId);
        ModifierOption existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ModifierOption", id));
        if (!groupId.equals(existing.getModifierGroupId())) {
            throw new ResourceNotFoundException("ModifierOption", id);
        }
        repository.deleteById(id);
    }

    // --- Merchant (user + group-scoped) operations ---

    @Transactional(readOnly = true)
    public PageResponse<ModifierOptionResponse> findByGroupForMerchant(Long userId, Long groupId, Pageable pageable) {
        modifierGroupService.requireGroupForMerchant(userId, groupId);
        return PageResponse.of(repository.findByModifierGroupId(groupId, pageable).map(DtoMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public ModifierOptionResponse getByIdForMerchant(Long userId, Long groupId, Long id) {
        modifierGroupService.requireGroupForMerchant(userId, groupId);
        ModifierOption opt = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ModifierOption", id));
        if (!groupId.equals(opt.getModifierGroupId())) {
            throw new ResourceNotFoundException("ModifierOption", id);
        }
        return DtoMapper.toResponse(opt);
    }

    @Transactional
    public ModifierOptionResponse createForMerchant(Long userId, Long groupId, ModifierOptionRequest request) {
        modifierGroupService.requireGroupForMerchant(userId, groupId);
        if (request.getModifierGroupId() != null && !request.getModifierGroupId().equals(groupId)) {
            throw new IllegalArgumentException("Modifier group ID must match path");
        }
        ModifierOption entity = DtoMapper.toEntity(request);
        entity.setModifierGroupId(groupId);
        return DtoMapper.toResponse(repository.save(entity));
    }

    @Transactional
    public ModifierOptionResponse updateForMerchant(Long userId, Long groupId, Long id, ModifierOptionRequest request) {
        modifierGroupService.requireGroupForMerchant(userId, groupId);
        ModifierOption existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ModifierOption", id));
        if (!groupId.equals(existing.getModifierGroupId())) {
            throw new ResourceNotFoundException("ModifierOption", id);
        }
        if (request.getModifierGroupId() != null && !request.getModifierGroupId().equals(groupId)) {
            throw new IllegalArgumentException("Modifier group ID must match path");
        }
        ModifierOption entity = DtoMapper.toEntity(request);
        entity.setId(id);
        entity.setModifierGroupId(groupId);
        return DtoMapper.toResponse(repository.save(entity));
    }

    @Transactional
    public void deleteForMerchant(Long userId, Long groupId, Long id) {
        modifierGroupService.requireGroupForMerchant(userId, groupId);
        ModifierOption existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ModifierOption", id));
        if (!groupId.equals(existing.getModifierGroupId())) {
            throw new ResourceNotFoundException("ModifierOption", id);
        }
        repository.deleteById(id);
    }
}