package com.habsida.store.controller.merchant;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.request.ModifierGroupRequest;
import com.habsida.store.dto.response.ModifierGroupResponse;
import com.habsida.store.entity.ModifierGroup;
import com.habsida.store.exception.ResourceNotFoundException;
import com.habsida.store.repository.ModifierGroupRepository;
import com.habsida.store.repository.UserStoreAccessRepository;
import com.habsida.store.security.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/merchant/modifier-groups")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
@Tag(name = "Modifiers", description = "Modifier groups, options, assign groups to products")
public class MerchantModifierGroupController {

    private final ModifierGroupRepository repository;
    private final UserStoreAccessRepository userStoreAccessRepository;

    private List<Long> getMerchantStoreIds(Long userId) {
        return userStoreAccessRepository.findByUserId(userId).stream()
                .map(usa -> usa.getStoreId())
                .filter(id -> id != null)
                .distinct()
                .toList();
    }

    @GetMapping
    public PageResponse<ModifierGroupResponse> findAll(
            @AuthenticationPrincipal AuthUser authUser,
            Pageable pageable) {
        List<Long> storeIds = getMerchantStoreIds(authUser.getId());
        if (storeIds.isEmpty()) {
            return PageResponse.of(org.springframework.data.domain.Page.empty(pageable));
        }
        return PageResponse.of(repository.findByStoreIdIn(storeIds, pageable).map(DtoMapper::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModifierGroupResponse> findById(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        List<Long> storeIds = getMerchantStoreIds(authUser.getId());
        ModifierGroup g = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("ModifierGroup", id));
        if (!storeIds.contains(g.getStoreId())) {
            throw new ResourceNotFoundException("ModifierGroup", id);
        }
        return ResponseEntity.ok(DtoMapper.toResponse(g));
    }

    @PostMapping
    public ResponseEntity<ModifierGroupResponse> create(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody ModifierGroupRequest request) {
        List<Long> storeIds = getMerchantStoreIds(authUser.getId());
        if (request.getStoreId() == null || !storeIds.contains(request.getStoreId())) {
            throw new ResourceNotFoundException("Store", request.getStoreId());
        }
        ModifierGroup entity = DtoMapper.toEntity(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.toResponse(repository.save(entity)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModifierGroupResponse> update(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id,
            @Valid @RequestBody ModifierGroupRequest request) {
        List<Long> storeIds = getMerchantStoreIds(authUser.getId());
        ModifierGroup existing = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("ModifierGroup", id));
        if (!storeIds.contains(existing.getStoreId())) {
            throw new ResourceNotFoundException("ModifierGroup", id);
        }
        if (request.getStoreId() != null && !storeIds.contains(request.getStoreId())) {
            throw new ResourceNotFoundException("Store", request.getStoreId());
        }
        ModifierGroup entity = DtoMapper.toEntity(request);
        entity.setId(id);
        return ResponseEntity.ok(DtoMapper.toResponse(repository.save(entity)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        List<Long> storeIds = getMerchantStoreIds(authUser.getId());
        ModifierGroup existing = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("ModifierGroup", id));
        if (!storeIds.contains(existing.getStoreId())) {
            throw new ResourceNotFoundException("ModifierGroup", id);
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
