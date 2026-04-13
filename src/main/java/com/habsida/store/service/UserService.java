package com.habsida.store.service;

import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.UserRequest;
import com.habsida.store.dto.response.UserResponse;
import com.habsida.store.entity.User;
import com.habsida.store.exception.ResourceNotFoundException;
import com.habsida.store.repository.UserRepository;
import com.habsida.store.spec.FilterSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Map<String, FilterSpecs.FilterMode> USER_FILTERS = Map.of(
            "email", FilterSpecs.FilterMode.CONTAINS_IGNORE_CASE
    );

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> findAll(Map<String, String> filter, Pageable pageable) {
        Specification<User> spec = FilterSpecs.from(filter, USER_FILTERS);
        Page<User> page = spec == null ? repository.findAll(pageable) : repository.findAll(spec, pageable);
        return PageResponse.of(page.map(DtoMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public Optional<UserResponse> findById(Long id) {
        return repository.findById(id).map(DtoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return DtoMapper.toResponse(
                repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id)));
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        User entity = DtoMapper.toEntity(request, passwordEncoder);
        return DtoMapper.toResponse(repository.save(entity));
    }

    @Transactional
    public Optional<UserResponse> update(Long id, UserRequest request) {
        return repository.findById(id)
                .map(existing -> {
                    DtoMapper.updateEntity(existing, request, passwordEncoder);
                    return DtoMapper.toResponse(repository.save(existing));
                });
    }

    @Transactional
    public UserResponse updateOrThrow(Long id, UserRequest request) {
        User existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        DtoMapper.updateEntity(existing, request, passwordEncoder);
        return DtoMapper.toResponse(repository.save(existing));
    }

    @Transactional
    public boolean delete(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }

    @Transactional
    public void deleteOrThrow(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("User", id);
        }
        repository.deleteById(id);
    }
}