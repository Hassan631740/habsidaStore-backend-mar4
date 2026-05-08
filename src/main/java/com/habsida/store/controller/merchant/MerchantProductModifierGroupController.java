package com.habsida.store.controller.merchant;

import com.habsida.store.dto.request.ProductModifierGroupRequest;
import com.habsida.store.dto.response.ProductModifierGroupResponse;
import com.habsida.store.security.AuthUser;
import com.habsida.store.service.ProductModifierGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/merchant/products/{productId}/modifier-groups")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
@Tag(name = "Modifiers", description = "Modifier groups, options, assign groups to products")
public class MerchantProductModifierGroupController {

    private final ProductModifierGroupService productModifierGroupService;

    @Operation(summary = "List modifier groups assigned to a product")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping
    public List<ProductModifierGroupResponse> list(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long productId,
            Pageable pageable) {
        return productModifierGroupService.listForMerchant(authUser.getId(), productId, pageable);
    }

    @Operation(summary = "Assign a modifier group to a product")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Assigned"), @ApiResponse(responseCode = "404", description = "Not found")})
    @PostMapping
    public ResponseEntity<ProductModifierGroupResponse> assign(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long productId,
            @Valid @RequestBody ProductModifierGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productModifierGroupService.assignForMerchant(authUser.getId(), productId, request));
    }

    @Operation(summary = "Unassign a modifier group from a product")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Removed"), @ApiResponse(responseCode = "404", description = "Not found")})
    @DeleteMapping("/{modifierGroupId}")
    public ResponseEntity<Void> unassign(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long productId,
            @PathVariable Long modifierGroupId) {
        productModifierGroupService.unassignForMerchant(authUser.getId(), productId, modifierGroupId);
        return ResponseEntity.noContent().build();
    }
}