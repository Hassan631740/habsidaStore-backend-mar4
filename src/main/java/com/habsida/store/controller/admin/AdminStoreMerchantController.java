package com.habsida.store.controller.admin;

import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.request.AssignMerchantRequest;
import com.habsida.store.dto.response.UserStoreAccessResponse;
import com.habsida.store.entity.UserStoreAccess;
import com.habsida.store.exception.ResourceNotFoundException;
import com.habsida.store.repository.StoreRepository;
import com.habsida.store.repository.UserRepository;
import com.habsida.store.repository.UserStoreAccessRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/**
 * Admin: assign or remove merchant (user) to/from a store. RBAC: gives the user access to manage that store's catalog.
 */
@RestController
@RequestMapping("/api/admin/stores/{storeId}/merchants")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Stores", description = "Store management (admin: any store; merchant: assigned stores)")
public class AdminStoreMerchantController {

    private final UserStoreAccessRepository userStoreAccessRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    @GetMapping
    public List<UserStoreAccessResponse> listAssigned(@PathVariable Long storeId) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", storeId);
        }
        return userStoreAccessRepository.findByStoreId(storeId).stream()
                .map(DtoMapper::toResponse)
                .toList();
    }

    @PostMapping
    public ResponseEntity<UserStoreAccessResponse> assignMerchant(
            @PathVariable Long storeId,
            @Valid @RequestBody AssignMerchantRequest request) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", storeId);
        }
        if (!userRepository.existsById(request.getUserId())) {
            throw new ResourceNotFoundException("User", request.getUserId());
        }
        if (userStoreAccessRepository.existsByUserIdAndStoreId(request.getUserId(), storeId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        UserStoreAccess entity = UserStoreAccess.builder()
                .userId(request.getUserId())
                .storeId(storeId)
                .build();
        UserStoreAccess saved = userStoreAccessRepository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.toResponse(saved));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> unassignMerchant(@PathVariable Long storeId, @PathVariable Long userId) {
        List<UserStoreAccess> list = userStoreAccessRepository.findByStoreId(storeId).stream()
                .filter(usa -> userId.equals(usa.getUserId()))
                .toList();
        if (list.isEmpty()) {
            throw new ResourceNotFoundException("User not assigned to this store");
        }
        list.forEach(userStoreAccessRepository::delete);
        return ResponseEntity.noContent().build();
    }
}
