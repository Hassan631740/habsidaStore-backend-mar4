package com.habsida.store.controller.merchant;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.request.ModifierOptionRequest;
import com.habsida.store.dto.response.ModifierOptionResponse;
import com.habsida.store.entity.ModifierGroup;
import com.habsida.store.entity.ModifierOption;
import com.habsida.store.exception.ResourceNotFoundException;
import com.habsida.store.repository.ModifierGroupRepository;
import com.habsida.store.repository.ModifierOptionRepository;
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
@RequestMapping("/api/merchant/modifier-groups/{groupId}/options")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
@Tag(name = "Modifiers", description = "Modifier groups, options, assign groups to products")
public class MerchantModifierOptionController {

    private final ModifierOptionRepository optionRepository;
    private final ModifierGroupRepository groupRepository;
    private final UserStoreAccessRepository userStoreAccessRepository;

    private List<Long> getMerchantStoreIds(Long userId) {
        return userStoreAccessRepository.findByUserId(userId).stream()
                .map(usa -> usa.getStoreId())
                .filter(id -> id != null)
                .distinct()
                .toList();
    }

    private void ensureGroupBelongsToMerchant(Long userId, Long groupId) {
        List<Long> storeIds = getMerchantStoreIds(userId);
        ModifierGroup g = groupRepository.findById(groupId).orElseThrow(() -> new ResourceNotFoundException("ModifierGroup", groupId));
        if (!storeIds.contains(g.getStoreId())) {
            throw new ResourceNotFoundException("ModifierGroup", groupId);
        }
    }

    @GetMapping
    public PageResponse<ModifierOptionResponse> findAll(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long groupId,
            Pageable pageable) {
        ensureGroupBelongsToMerchant(authUser.getId(), groupId);
        return PageResponse.of(optionRepository.findByModifierGroupId(groupId, pageable).map(DtoMapper::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModifierOptionResponse> findById(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long groupId,
            @PathVariable Long id) {
        ensureGroupBelongsToMerchant(authUser.getId(), groupId);
        ModifierOption opt = optionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("ModifierOption", id));
        if (!groupId.equals(opt.getModifierGroupId())) {
            throw new ResourceNotFoundException("ModifierOption", id);
        }
        return ResponseEntity.ok(DtoMapper.toResponse(opt));
    }

    @PostMapping
    public ResponseEntity<ModifierOptionResponse> create(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long groupId,
            @Valid @RequestBody ModifierOptionRequest request) {
        ensureGroupBelongsToMerchant(authUser.getId(), groupId);
        if (request.getModifierGroupId() != null && !request.getModifierGroupId().equals(groupId)) {
            throw new IllegalArgumentException("Modifier group ID must match path");
        }
        ModifierOption entity = DtoMapper.toEntity(request);
        entity.setModifierGroupId(groupId);
        return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.toResponse(optionRepository.save(entity)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModifierOptionResponse> update(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long groupId,
            @PathVariable Long id,
            @Valid @RequestBody ModifierOptionRequest request) {
        ensureGroupBelongsToMerchant(authUser.getId(), groupId);
        ModifierOption existing = optionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("ModifierOption", id));
        if (!groupId.equals(existing.getModifierGroupId())) {
            throw new ResourceNotFoundException("ModifierOption", id);
        }
        if (request.getModifierGroupId() != null && !request.getModifierGroupId().equals(groupId)) {
            throw new IllegalArgumentException("Modifier group ID must match path");
        }
        ModifierOption entity = DtoMapper.toEntity(request);
        entity.setId(id);
        entity.setModifierGroupId(groupId);
        return ResponseEntity.ok(DtoMapper.toResponse(optionRepository.save(entity)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long groupId,
            @PathVariable Long id) {
        ensureGroupBelongsToMerchant(authUser.getId(), groupId);
        ModifierOption existing = optionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("ModifierOption", id));
        if (!groupId.equals(existing.getModifierGroupId())) {
            throw new ResourceNotFoundException("ModifierOption", id);
        }
        optionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
