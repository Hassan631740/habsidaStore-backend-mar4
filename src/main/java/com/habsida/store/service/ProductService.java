package com.habsida.store.service;

import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.ProductRequest;
import com.habsida.store.dto.response.ProductResponse;
import com.habsida.store.entity.Product;
import com.habsida.store.entity.UserStoreAccess;
import com.habsida.store.exception.ResourceNotFoundException;
import com.habsida.store.repository.ProductRepository;
import com.habsida.store.repository.StoreRepository;
import com.habsida.store.repository.UserStoreAccessRepository;
import com.habsida.store.spec.FilterSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    /** List filters: category (categoryId), availability (availableForOrder), text search (q = name/description). */
    private static final Map<String, FilterSpecs.FilterMode> PRODUCT_FILTERS = Map.of(
            "categoryId", FilterSpecs.FilterMode.EQUALS_LONG,
            "storeId", FilterSpecs.FilterMode.EQUALS_LONG,
            "availableForOrder", FilterSpecs.FilterMode.EQUALS_BOOLEAN
    );

    private final ProductRepository repository;
    private final StoreRepository storeRepository;
    private final UserStoreAccessRepository userStoreAccessRepository;

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> findAll(Pageable pageable, Map<String, String> filter) {
        Specification<Product> spec = productListSpec(filter);
        Page<Product> page = spec == null ? repository.findAll(pageable) : repository.findAll(spec, pageable);
        return PageResponse.of(page.map(DtoMapper::toResponse));
    }

    /** List products for given stores only (e.g. merchant's stores), with same filters as findAll. */
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> findAllForStores(List<Long> storeIds, Pageable pageable, Map<String, String> filter) {
        if (storeIds == null || storeIds.isEmpty()) {
            return PageResponse.of(Page.empty(pageable));
        }
        Specification<Product> storeSpec = (root, query, cb) -> root.get("storeId").in(storeIds);
        Map<String, String> filterWithoutStore = filter != null ? new java.util.HashMap<>(filter) : new java.util.HashMap<>();
        filterWithoutStore.remove("storeId");
        Specification<Product> rest = productListSpec(filterWithoutStore);
        Specification<Product> spec = rest == null ? storeSpec : storeSpec.and(rest);
        return PageResponse.of(repository.findAll(spec, pageable).map(DtoMapper::toResponse));
    }

    private static Specification<Product> productListSpec(Map<String, String> filter) {
        Specification<Product> filterSpec = FilterSpecs.from(filter, PRODUCT_FILTERS);
        String q = filter != null ? filter.get("q") : null;
        boolean hasSearch = q != null && !q.isBlank();
        if (!hasSearch && filterSpec == null) {
            return null;
        }
        if (!hasSearch) {
            return filterSpec;
        }
        String searchTerm = "%" + q.trim().toLowerCase() + "%";
        Specification<Product> searchSpec = (root, query, cb) -> {
            Predicate nameLike = cb.like(cb.lower(root.get("name")), searchTerm);
            Predicate descLike = cb.like(cb.lower(root.get("description")), searchTerm);
            return cb.or(nameLike, descLike);
        };
        return filterSpec == null ? searchSpec : filterSpec.and(searchSpec);
    }

    @Transactional(readOnly = true)
    public Optional<ProductResponse> findById(Long id) {
        return repository.findById(id).map(DtoMapper::toResponse);
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product entity = DtoMapper.toEntity(request);
        Product saved = repository.save(entity);
        return DtoMapper.toResponse(saved);
    }

    @Transactional
    public Optional<ProductResponse> update(Long id, ProductRequest request) {
        if (!repository.existsById(id)) {
            return Optional.empty();
        }
        Product entity = DtoMapper.toEntity(request);
        entity.setId(id);
        return Optional.of(DtoMapper.toResponse(repository.save(entity)));
    }

    /** Pause or resume ordering for a product (sets availableForOrder = !paused). */
    @Transactional
    public Optional<ProductResponse> setOrderingPaused(Long id, boolean paused) {
        return repository.findById(id)
                .map(p -> {
                    p.setAvailableForOrder(!paused);
                    return DtoMapper.toResponse(repository.save(p));
                });
    }

    @Transactional
    public boolean deleteById(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }

    // --- Admin (store-scoped) operations ---

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> findAllForStore(Long storeId, Long categoryId, Boolean availableForOrder, String q, Pageable pageable) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", storeId);
        }
        Map<String, String> filter = new java.util.HashMap<>();
        filter.put("storeId", String.valueOf(storeId));
        if (categoryId != null) filter.put("categoryId", String.valueOf(categoryId));
        if (availableForOrder != null) filter.put("availableForOrder", String.valueOf(availableForOrder));
        if (q != null && !q.isBlank()) filter.put("q", q);
        return findAll(pageable, filter);
    }

    @Transactional(readOnly = true)
    public Optional<ProductResponse> findByIdForStore(Long storeId, Long id) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", storeId);
        }
        return findById(id).filter(p -> storeId.equals(p.getStoreId()));
    }

    @Transactional
    public ProductResponse createForStore(Long storeId, ProductRequest request) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", storeId);
        }
        if (request.getStoreId() != null && !request.getStoreId().equals(storeId)) {
            throw new IllegalArgumentException("Store ID must match path");
        }
        ProductRequest withStore = ProductRequest.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .categoryId(request.getCategoryId())
                .storeId(storeId)
                .availableForOrder(request.getAvailableForOrder() != null ? request.getAvailableForOrder() : true)
                .build();
        return create(withStore);
    }

    @Transactional
    public Optional<ProductResponse> updateForStore(Long storeId, Long id, ProductRequest request) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", storeId);
        }
        if (request.getStoreId() != null && !request.getStoreId().equals(storeId)) {
            throw new IllegalArgumentException("Store ID must match path");
        }
        ProductRequest withStore = ProductRequest.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .categoryId(request.getCategoryId())
                .storeId(storeId)
                .availableForOrder(request.getAvailableForOrder() != null ? request.getAvailableForOrder() : true)
                .build();
        return update(id, withStore).filter(p -> storeId.equals(p.getStoreId()));
    }

    @Transactional
    public boolean deleteByIdForStore(Long storeId, Long id) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", storeId);
        }
        if (findById(id).filter(p -> storeId.equals(p.getStoreId())).isEmpty()) {
            return false;
        }
        return deleteById(id);
    }

    // --- Merchant (user-scoped) operations ---

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> findAllForMerchantUser(Long userId, Long categoryId, Boolean availableForOrder, String q, Pageable pageable) {
        List<Long> storeIds = getMerchantStoreIds(userId);
        Map<String, String> filter = new java.util.HashMap<>();
        if (categoryId != null) filter.put("categoryId", String.valueOf(categoryId));
        if (availableForOrder != null) filter.put("availableForOrder", String.valueOf(availableForOrder));
        if (q != null && !q.isBlank()) filter.put("q", q);
        return findAllForStores(storeIds, pageable, filter);
    }

    @Transactional(readOnly = true)
    public Optional<ProductResponse> findByIdForMerchant(Long userId, Long id) {
        List<Long> storeIds = getMerchantStoreIds(userId);
        return findById(id).filter(p -> p.getStoreId() != null && storeIds.contains(p.getStoreId()));
    }

    @Transactional
    public ProductResponse createForMerchant(Long userId, ProductRequest request) {
        List<Long> storeIds = getMerchantStoreIds(userId);
        if (request.getStoreId() == null || !storeIds.contains(request.getStoreId())) {
            throw new ResourceNotFoundException("Store", request.getStoreId());
        }
        return create(request);
    }

    @Transactional
    public Optional<ProductResponse> updateForMerchant(Long userId, Long id, ProductRequest request) {
        List<Long> storeIds = getMerchantStoreIds(userId);
        if (request.getStoreId() != null && !storeIds.contains(request.getStoreId())) {
            throw new ResourceNotFoundException("Store", request.getStoreId());
        }
        return findById(id)
                .filter(p -> storeIds.contains(p.getStoreId()))
                .flatMap(p -> update(id, request));
    }

    @Transactional
    public Optional<ProductResponse> setOrderingPausedForMerchant(Long userId, Long id, boolean paused) {
        List<Long> storeIds = getMerchantStoreIds(userId);
        return findById(id)
                .filter(p -> storeIds.contains(p.getStoreId()))
                .flatMap(p -> setOrderingPaused(id, paused));
    }

    @Transactional
    public boolean deleteByIdForMerchant(Long userId, Long id) {
        List<Long> storeIds = getMerchantStoreIds(userId);
        if (findById(id).filter(p -> storeIds.contains(p.getStoreId())).isEmpty()) {
            return false;
        }
        return deleteById(id);
    }

    private List<Long> getMerchantStoreIds(Long userId) {
        return userStoreAccessRepository.findByUserId(userId).stream()
                .map(UserStoreAccess::getStoreId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
}
