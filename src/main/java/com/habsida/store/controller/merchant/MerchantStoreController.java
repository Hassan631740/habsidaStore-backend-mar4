package com.habsida.store.controller.merchant;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.response.StoreResponse;
import com.habsida.store.exception.ResourceNotFoundException;
import com.habsida.store.repository.StoreRepository;
import com.habsida.store.repository.UserStoreAccessRepository;
import com.habsida.store.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/**
 * Merchant-scoped store access. Lists only stores the current user has access to. Requires ROLE_MERCHANT.
 */
@RestController
@RequestMapping("/api/merchant/stores")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
@Tag(name = "Stores", description = "Store management (admin: any store; merchant: assigned stores)")
public class MerchantStoreController {

    private final StoreRepository storeRepository;
    private final UserStoreAccessRepository userStoreAccessRepository;

    @GetMapping
    public PageResponse<StoreResponse> findMyStores(
            @AuthenticationPrincipal AuthUser authUser,
            Pageable pageable) {
        List<Long> storeIds = userStoreAccessRepository.findByUserId(authUser.getId()).stream()
                .map(usa -> usa.getStoreId())
                .filter(id -> id != null)
                .distinct()
                .toList();
        if (storeIds.isEmpty()) {
            return PageResponse.of(org.springframework.data.domain.Page.empty(pageable));
        }
        return PageResponse.of(
                storeRepository.findByIdIn(storeIds, pageable).map(DtoMapper::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreResponse> findById(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id) {
        List<Long> allowedStoreIds = userStoreAccessRepository.findByUserId(authUser.getId()).stream()
                .map(usa -> usa.getStoreId())
                .filter(sid -> sid != null)
                .distinct()
                .toList();
        if (!allowedStoreIds.contains(id)) {
            throw new ResourceNotFoundException("Store", id);
        }
        return ResponseEntity.ok(DtoMapper.toResponse(
                storeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Store", id))));
    }
}
