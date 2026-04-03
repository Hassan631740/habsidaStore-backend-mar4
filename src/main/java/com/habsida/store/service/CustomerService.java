package com.habsida.store.service;

import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.AdminCustomerCreateRequest;
import com.habsida.store.dto.request.CustomerRequest;
import com.habsida.store.dto.request.CustomerStatusUpdateRequest;
import com.habsida.store.dto.response.AddressResponse;
import com.habsida.store.dto.response.AdminCustomerDetailResponse;
import com.habsida.store.dto.response.CustomerResponse;
import com.habsida.store.entity.Address;
import com.habsida.store.entity.Customer;
import com.habsida.store.entity.CustomerAddress;
import com.habsida.store.entity.UserStoreAccess;
import com.habsida.store.enums.CustomerStatus;
import com.habsida.store.exception.ResourceNotFoundException;
import com.habsida.store.repository.AddressRepository;
import com.habsida.store.repository.CustomerAddressRepository;
import com.habsida.store.repository.CustomerRepository;
import com.habsida.store.repository.UserStoreAccessRepository;
import com.habsida.store.spec.FilterSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

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
    private final UserStoreAccessRepository userStoreAccessRepository;

    // --- Basic CRUD (used by CustomerController) ---

    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> findAll(Pageable pageable) {
        return PageResponse.of(customerRepository.findAll(pageable).map(DtoMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public Optional<CustomerResponse> findById(Long id) {
        return customerRepository.findById(id).map(DtoMapper::toResponse);
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        Customer entity = DtoMapper.toEntity(request);
        return DtoMapper.toResponse(customerRepository.save(entity));
    }

    @Transactional
    public Optional<CustomerResponse> update(Long id, CustomerRequest request) {
        return customerRepository.findById(id)
                .map(existing -> {
                    existing.setUserId(request.getUserId());
                    existing.setFirstName(request.getFirstName());
                    existing.setLastName(request.getLastName());
                    existing.setPhone(request.getPhone());
                    if (request.getStatus() != null) {
                        existing.setStatus(request.getStatus());
                    } else if (existing.getStatus() == null) {
                        existing.setStatus(CustomerStatus.ACTIVE);
                    }
                    return DtoMapper.toResponse(customerRepository.save(existing));
                });
    }

    @Transactional
    public boolean delete(Long id) {
        if (!customerRepository.existsById(id)) {
            return false;
        }
        customerRepository.deleteById(id);
        return true;
    }

    // --- Admin operations ---

    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> findAll(Map<String, String> filter, Pageable pageable) {
        Specification<Customer> spec = FilterSpecs.from(filter, CUSTOMER_FILTERS);
        Page<Customer> page = spec == null
                ? customerRepository.findAll(pageable)
                : customerRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(DtoMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public AdminCustomerDetailResponse getDetailById(Long id) {
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
        List<AddressResponse> addresses = customerAddressRepository.findByCustomerIdOrderByIdAsc(id).stream()
                .map(CustomerAddress::getAddressId)
                .map(addressRepository::findById)
                .filter(Optional::isPresent)
                .map(opt -> DtoMapper.toResponse(opt.get()))
                .collect(Collectors.toList());
        return toDetail(c, addresses);
    }

    @Transactional
    public AdminCustomerDetailResponse createWithAddresses(AdminCustomerCreateRequest request) {
        CustomerStatus status = request.getStatus() != null ? request.getStatus() : CustomerStatus.ACTIVE;
        Customer customer = Customer.builder()
                .userId(request.getUserId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .status(status)
                .build();
        customer = customerRepository.save(customer);

        List<AddressResponse> addressResponses = new ArrayList<>();
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
        return toDetail(customer, addressResponses);
    }

    @Transactional
    public CustomerResponse updateStatus(Long id, CustomerStatusUpdateRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
        customer.setStatus(request.getStatus());
        return DtoMapper.toResponse(customerRepository.save(customer));
    }

    // --- Merchant operations ---

    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> findCustomersWhoOrderedFromMerchant(Long userId, Long storeId, Pageable pageable) {
        List<Long> storeIds = getMerchantStoreIds(userId);
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

    private List<Long> getMerchantStoreIds(Long userId) {
        return userStoreAccessRepository.findByUserId(userId).stream()
                .map(UserStoreAccess::getStoreId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private static AdminCustomerDetailResponse toDetail(Customer c, List<AddressResponse> addresses) {
        return AdminCustomerDetailResponse.builder()
                .id(c.getId())
                .userId(c.getUserId())
                .firstName(c.getFirstName())
                .lastName(c.getLastName())
                .phone(c.getPhone())
                .status(c.getStatus())
                .addresses(addresses)
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}