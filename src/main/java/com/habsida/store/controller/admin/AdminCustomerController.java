package com.habsida.store.controller.admin;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.AdminCustomerCreateRequest;
import com.habsida.store.dto.request.CustomerStatusUpdateRequest;
import com.habsida.store.dto.response.AdminCustomerDetailResponse;
import com.habsida.store.dto.response.CustomerResponse;
import com.habsida.store.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/customers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Customers", description = "Admin: customer management and status control")
public class AdminCustomerController {

    private final CustomerService customerService;

    @Operation(summary = "List all customers (filterable)")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping
    public PageResponse<CustomerResponse> list(
            @RequestParam(required = false) Map<String, String> filter,
            Pageable pageable) {
        return customerService.findAll(filter, pageable);
    }

    @Operation(summary = "Get customer details by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AdminCustomerDetailResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getDetailById(id));
    }

    @Operation(summary = "Create a customer with addresses")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping
    public ResponseEntity<AdminCustomerDetailResponse> create(@Valid @RequestBody AdminCustomerCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.createWithAddresses(request));
    }

    @Operation(summary = "Update customer status (ACTIVE / SUSPENDED)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Updated"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<CustomerResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody CustomerStatusUpdateRequest request) {
        return ResponseEntity.ok(customerService.updateStatus(id, request));
    }
}