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
import com.habsida.store.exception.ResourceNotFoundException;
import com.habsida.store.repository.AddressRepository;
import com.habsida.store.repository.CustomerAddressRepository;
import com.habsida.store.repository.CustomerRepository;
import com.habsida.store.service.AdminCustomerService;
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
import java.util.Objects;
import java.util.function.Function;
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
            "status", FilterSpecs.FilterMode.EQUALS_CUSTOMER_STATUS,
            "userId", FilterSpecs.FilterMode.EQUALS_LONG
    );

    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final CustomerAddressRepository customerAddressRepository;
    private final AdminCustomerService adminCustomerService;

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
        List<CustomerAddress> links = customerAddressRepository.findByCustomerIdOrderByIdAsc(id);
        List<Long> addressIds = links.stream().map(CustomerAddress::getAddressId).toList();
        Map<Long, Address> addressById = addressRepository.findAllById(addressIds).stream()
                .collect(Collectors.toMap(Address::getId, Function.identity()));
        List<AddressResponse> addresses = links.stream()
                .map(CustomerAddress::getAddressId)
                .map(addressById::get)
                .filter(Objects::nonNull)
                .map(DtoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(adminCustomerService.toDetail(c, addresses));
    }

    @PostMapping
    public ResponseEntity<AdminCustomerDetailResponse> create(@Valid @RequestBody AdminCustomerCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminCustomerService.createCustomer(request));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<CustomerResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody CustomerStatusUpdateRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
        customer.setStatus(request.getStatus());
        return ResponseEntity.ok(DtoMapper.toResponse(customerRepository.save(customer)));
    }
}
