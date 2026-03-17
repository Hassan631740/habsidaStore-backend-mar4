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

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final Map<String, FilterSpecs.FilterMode> PRODUCT_FILTERS = Map.of(
            "name", FilterSpecs.FilterMode.CONTAINS_IGNORE_CASE,
            "categoryId", FilterSpecs.FilterMode.EQUALS_LONG,
            "storeId", FilterSpecs.FilterMode.EQUALS_LONG
    );

    private final ProductRepository repository;

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> findAll(Pageable pageable, Map<String, String> filter) {
        Specification<Product> spec = FilterSpecs.from(filter, PRODUCT_FILTERS);
        Page<Product> page = spec == null ? repository.findAll(pageable) : repository.findAll(spec, pageable);
        return PageResponse.of(page.map(DtoMapper::toResponse));
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

    @Transactional
    public boolean deleteById(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }
}
