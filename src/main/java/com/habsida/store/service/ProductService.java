package com.habsida.store.service;

import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.ProductRequest;
import com.habsida.store.dto.response.ProductResponse;
import com.habsida.store.entity.Product;
import com.habsida.store.repository.ProductRepository;
import com.habsida.store.spec.FilterSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.Map;
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

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> findAll(Pageable pageable, Map<String, String> filter) {
        Specification<Product> spec = productListSpec(filter);
        Page<Product> page = spec == null ? repository.findAll(pageable) : repository.findAll(spec, pageable);
        return PageResponse.of(page.map(DtoMapper::toResponse));
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
}
