package com.habsida.store.controller.merchant;

import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.response.CustomerResponse;
import com.habsida.store.exception.ResourceNotFoundException;
import com.habsida.store.repository.CustomerRepository;
import com.habsida.store.repository.UserStoreAccessRepository;
import com.habsida.store.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Customers who have placed at least one order containing products from the merchant's store(s).
 */
@RestController
@RequestMapping("/api/merchant/customers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
public class MerchantCustomerController {

    private final CustomerRepository customerRepository;
    private final UserStoreAccessRepository userStoreAccessRepository;

    @GetMapping
    public PageResponse<CustomerResponse> listCustomersWhoOrdered(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(required = false) Long storeId,
            Pageable pageable) {
        List<Long> storeIds = userStoreAccessRepository.findByUserId(authUser.getId()).stream()
                .map(usa -> usa.getStoreId())
                .filter(id -> id != null)
                .distinct()
                .toList();
        if (storeIds.isEmpty()) {
            return PageResponse.of(Page.empty(pageable));
        }
        if (storeId != null) {
            if (!storeIds.contains(storeId)) {
                throw new ResourceNotFoundException("Store", storeId);
            }
            return PageResponse.of(
                    customerRepository.findDistinctCustomersWhoOrderedFromStore(storeId, pageable)
                            .map(DtoMapper::toResponse));
        }
        return PageResponse.of(
                customerRepository.findDistinctCustomersWhoOrderedFromStores(storeIds, pageable)
                        .map(DtoMapper::toResponse));
    }
}
