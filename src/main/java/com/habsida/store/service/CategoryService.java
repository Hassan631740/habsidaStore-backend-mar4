package com.habsida.store.service;

import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.CategoryRequest;
import com.habsida.store.dto.response.CategoryResponse;
import com.habsida.store.entity.Category;
import com.habsida.store.entity.UserStoreAccess;
import com.habsida.store.exception.ResourceNotFoundException;
import com.habsida.store.repository.CategoryRepository;
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
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;
    private final UserStoreAccessRepository userStoreAccessRepository;

    // --- Basic CRUD (used by CategoryController) ---

    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> findAll(Pageable pageable) {
        return PageResponse.of(categoryRepository.findAll(pageable).map(DtoMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public Optional<CategoryResponse> findById(Long id) {
        return categoryRepository.findById(id).map(DtoMapper::toResponse);
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        Category entity = DtoMapper.toEntity(request);
        return DtoMapper.toResponse(categoryRepository.save(entity));
    }

    @Transactional
    public Optional<CategoryResponse> update(Long id, CategoryRequest request) {
        if (!categoryRepository.existsById(id)) {
            return Optional.empty();
        }
        Category entity = DtoMapper.toEntity(request);
        entity.setId(id);
        return Optional.of(DtoMapper.toResponse(categoryRepository.save(entity)));
    }

    @Transactional
    public boolean delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            return false;
        }
        categoryRepository.deleteById(id);
        return true;
    }

    // --- Admin (store-scoped) operations ---

    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> findByStoreId(Long storeId, Pageable pageable) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", storeId);
        }
        return PageResponse.of(categoryRepository.findByStoreId(storeId, pageable).map(DtoMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public CategoryResponse getByIdForStore(Long storeId, Long id) {
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        if (cat.getStoreId() == null || !cat.getStoreId().equals(storeId)) {
            throw new ResourceNotFoundException("Category", id);
        }
        return DtoMapper.toResponse(cat);
    }

    @Transactional
    public CategoryResponse createForStore(Long storeId, CategoryRequest request) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", storeId);
        }
        if (request.getStoreId() != null && !request.getStoreId().equals(storeId)) {
            throw new IllegalArgumentException("Store ID in path and body must match");
        }
        Category entity = DtoMapper.toEntity(request);
        entity.setStoreId(storeId);
        return DtoMapper.toResponse(categoryRepository.save(entity));
    }

    @Transactional
    public CategoryResponse updateForStore(Long storeId, Long id, CategoryRequest request) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        if (existing.getStoreId() == null || !existing.getStoreId().equals(storeId)) {
            throw new ResourceNotFoundException("Category", id);
        }
        if (request.getStoreId() != null && !request.getStoreId().equals(storeId)) {
            throw new IllegalArgumentException("Store ID in path and body must match");
        }
        Category entity = DtoMapper.toEntity(request);
        entity.setId(id);
        entity.setStoreId(storeId);
        return DtoMapper.toResponse(categoryRepository.save(entity));
    }

    @Transactional
    public void deleteForStore(Long storeId, Long id) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        if (existing.getStoreId() == null || !existing.getStoreId().equals(storeId)) {
            throw new ResourceNotFoundException("Category", id);
        }
        categoryRepository.deleteById(id);
    }

    // --- Merchant (user-scoped) operations ---

    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> findAllForMerchant(Long userId, Pageable pageable) {
        List<Long> storeIds = getMerchantStoreIds(userId);
        if (storeIds.isEmpty()) {
            return PageResponse.of(Page.empty(pageable));
        }
        return PageResponse.of(categoryRepository.findByStoreIdIn(storeIds, pageable).map(DtoMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public CategoryResponse getByIdForMerchant(Long userId, Long id) {
        List<Long> storeIds = getMerchantStoreIds(userId);
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        if (cat.getStoreId() == null || !storeIds.contains(cat.getStoreId())) {
            throw new ResourceNotFoundException("Category", id);
        }
        return DtoMapper.toResponse(cat);
    }

    @Transactional
    public CategoryResponse createForMerchant(Long userId, CategoryRequest request) {
        List<Long> storeIds = getMerchantStoreIds(userId);
        Long storeId = request.getStoreId();
        if (storeId == null || !storeIds.contains(storeId)) {
            throw new ResourceNotFoundException("Store", storeId);
        }
        Category entity = DtoMapper.toEntity(request);
        entity.setStoreId(storeId);
        return DtoMapper.toResponse(categoryRepository.save(entity));
    }

    @Transactional
    public CategoryResponse updateForMerchant(Long userId, Long id, CategoryRequest request) {
        List<Long> storeIds = getMerchantStoreIds(userId);
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        if (existing.getStoreId() == null || !storeIds.contains(existing.getStoreId())) {
            throw new ResourceNotFoundException("Category", id);
        }
        Long storeId = request.getStoreId() != null ? request.getStoreId() : existing.getStoreId();
        if (!storeIds.contains(storeId)) {
            throw new ResourceNotFoundException("Store", storeId);
        }
        Category entity = DtoMapper.toEntity(request);
        entity.setId(id);
        entity.setStoreId(storeId);
        return DtoMapper.toResponse(categoryRepository.save(entity));
    }

    @Transactional
    public void deleteForMerchant(Long userId, Long id) {
        List<Long> storeIds = getMerchantStoreIds(userId);
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        if (existing.getStoreId() == null || !storeIds.contains(existing.getStoreId())) {
            throw new ResourceNotFoundException("Category", id);
        }
        categoryRepository.deleteById(id);
    }

    private List<Long> getMerchantStoreIds(Long userId) {
        return userStoreAccessRepository.findByUserId(userId).stream()
                .map(UserStoreAccess::getStoreId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
}