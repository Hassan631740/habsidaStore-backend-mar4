package com.habsida.store.controller;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.request.UserRequest;
import com.habsida.store.dto.response.UserResponse;
import com.habsida.store.entity.User;
import com.habsida.store.repository.UserRepository;
import com.habsida.store.spec.FilterSpecs;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private static final Map<String, FilterSpecs.FilterMode> USER_FILTERS = Map.of(
            "email", FilterSpecs.FilterMode.CONTAINS_IGNORE_CASE
    );

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public PageResponse<UserResponse> findAll(
            @RequestParam(required = false) Map<String, String> filter,
            Pageable pageable) {
        Specification<User> spec = FilterSpecs.from(filter, USER_FILTERS);
        Page<User> page = spec == null ? repository.findAll(pageable) : repository.findAll(spec, pageable);
        return PageResponse.of(page.map(DtoMapper::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable Long id) {
        return repository.findById(id)
                .map(DtoMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        User entity = DtoMapper.toEntity(request, passwordEncoder);
        User saved = repository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        User existing = repository.findById(id).orElse(null);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        DtoMapper.updateEntity(existing, request, passwordEncoder);
        return ResponseEntity.ok(DtoMapper.toResponse(repository.save(existing)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
