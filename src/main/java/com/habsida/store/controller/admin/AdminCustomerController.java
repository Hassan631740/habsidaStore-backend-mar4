package com.habsida.store.controller.admin;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.AdminCustomerCreateRequest;
import com.habsida.store.dto.request.CustomerStatusUpdateRequest;
import com.habsida.store.dto.response.AdminCustomerDetailResponse;
import com.habsida.store.dto.response.CustomerResponse;
import com.habsida.store.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin customer management and {@code customer_addresses}. Requires ROLE_ADMIN.
 */
@RestController
@RequestMapping("/api/admin/customers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCustomerController {

    private final CustomerService customerService;

    @GetMapping
    public PageResponse<CustomerResponse> list(
            @RequestParam(required = false) Map<String, String> filter,
            Pageable pageable) {
        return customerService.findAll(filter, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminCustomerDetailResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getDetailById(id));
    }

    @PostMapping
    public ResponseEntity<AdminCustomerDetailResponse> create(@Valid @RequestBody AdminCustomerCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.createWithAddresses(request));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<CustomerResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody CustomerStatusUpdateRequest request) {
        return ResponseEntity.ok(customerService.updateStatus(id, request));
    }
}