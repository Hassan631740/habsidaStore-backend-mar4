package com.habsida.store.controller.admin;

import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.AdminCustomerCreateRequest;
import com.habsida.store.dto.request.CustomerStatusUpdateRequest;
import com.habsida.store.dto.response.AddressResponse;
import com.habsida.store.dto.response.AdminCustomerDetailResponse;
import com.habsida.store.dto.response.CustomerResponse;
import com.habsida.store.entity.Address;
import com.habsida.store.entity.Customer;
import com.habsida.store.entity.CustomerAddress;
import com.habsida.store.enums.CustomerStatus;
import com.habsida.store.exception.ResourceNotFoundException;
import com.habsida.store.repository.AddressRepository;
import com.habsida.store.repository.CustomerAddressRepository;
import com.habsida.store.repository.CustomerRepository;
import com.habsida.store.spec.FilterSpecs;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin customer management and {@code customer_addresses}. Requires ROLE_ADMIN.
 */
@RestController
@RequestMapping("/api/admin/customers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCustomerController {

    private static final Map<String, FilterSpecs.FilterMode> CUSTOMER_FILTERS = Map.of(
            "firstName", FilterSpecs.FilterMode.CONTAINS_IGNORE_CASE,
            "lastName", FilterSpecs.FilterMode.CONTAINS_IGNORE_CASE,
            "phone", FilterSpecs.FilterMode.CONTAINS_IGNORE_CASE,
            "status", FilterSpecs.FilterMode.EQUALS,
            "userId", FilterSpecs.FilterMode.EQUALS_LONG
    );

    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final CustomerAddressRepository customerAddressRepository;

    @GetMapping
    public PageResponse<CustomerResponse> list(
            @RequestParam(required = false) Map<String, String> filter,
            Pageable pageable) {
        Specification<Customer> spec = FilterSpecs.from(filter, CUSTOMER_FILTERS);
        Page<Customer> page = spec == null ? customerRepository.findAll(pageable) : customerRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(DtoMapper::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminCustomerDetailResponse> getById(@PathVariable Long id) {
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
        List<AddressResponse> addresses = customerAddressRepository.findByCustomerIdOrderByIdAsc(id).stream()
                .map(CustomerAddress::getAddressId)
                .map(addressRepository::findById)
                .filter(opt -> opt.isPresent())
                .map(opt -> DtoMapper.toResponse(opt.get()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(toDetail(c, addresses));
    }

    @PostMapping
    public ResponseEntity<AdminCustomerDetailResponse> create(@Valid @RequestBody AdminCustomerCreateRequest request) {
        CustomerStatus st = request.getStatus() != null ? request.getStatus() : CustomerStatus.ACTIVE;
        Customer customer = Customer.builder()
                .userId(request.getUserId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .status(st.name())
                .build();
        customer = customerRepository.save(customer);

        List<AddressResponse> addressResponses = new java.util.ArrayList<>();
        if (request.getAddresses() != null) {
            for (var ar : request.getAddresses()) {
                Address addr = addressRepository.save(DtoMapper.toEntity(ar));
                customerAddressRepository.save(CustomerAddress.builder()
                        .customerId(customer.getId())
                        .addressId(addr.getId())
                        .build());
                addressResponses.add(DtoMapper.toResponse(addr));
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(toDetail(customer, addressResponses));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<CustomerResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody CustomerStatusUpdateRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
        customer.setStatus(request.getStatus().name());
        return ResponseEntity.ok(DtoMapper.toResponse(customerRepository.save(customer)));
    }

    private static CustomerStatus parseCustomerStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return CustomerStatus.ACTIVE;
        }
        try {
            return CustomerStatus.valueOf(raw);
        } catch (IllegalArgumentException e) {
            return CustomerStatus.ACTIVE;
        }
    }

    private static AdminCustomerDetailResponse toDetail(Customer c, List<AddressResponse> addresses) {
        return AdminCustomerDetailResponse.builder()
                .id(c.getId())
                .userId(c.getUserId())
                .firstName(c.getFirstName())
                .lastName(c.getLastName())
                .phone(c.getPhone())
                .status(parseCustomerStatus(c.getStatus()))
                .addresses(addresses)
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
