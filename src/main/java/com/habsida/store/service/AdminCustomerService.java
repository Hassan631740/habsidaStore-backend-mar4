package com.habsida.store.service;

import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.request.AdminCustomerCreateRequest;
import com.habsida.store.dto.request.AddressRequest;
import com.habsida.store.dto.response.AddressResponse;
import com.habsida.store.dto.response.AdminCustomerDetailResponse;
import com.habsida.store.entity.Address;
import com.habsida.store.entity.Customer;
import com.habsida.store.entity.CustomerAddress;
import com.habsida.store.enums.CustomerStatus;
import com.habsida.store.repository.AddressRepository;
import com.habsida.store.repository.CustomerAddressRepository;
import com.habsida.store.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCustomerService {

    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final CustomerAddressRepository customerAddressRepository;

    @Transactional
    public AdminCustomerDetailResponse createCustomer(AdminCustomerCreateRequest request) {
        CustomerStatus st = request.getStatus() != null ? request.getStatus() : CustomerStatus.ACTIVE;
        Customer customer = Customer.builder()
                .userId(request.getUserId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .status(st)
                .build();
        customer = customerRepository.save(customer);

        List<AddressResponse> addressResponses = new ArrayList<>();
        if (request.getAddresses() != null) {
            for (AddressRequest ar : request.getAddresses()) {
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

    public AdminCustomerDetailResponse toDetail(Customer c, List<AddressResponse> addresses) {
        return AdminCustomerDetailResponse.builder()
                .id(c.getId())
                .userId(c.getUserId())
                .firstName(c.getFirstName())
                .lastName(c.getLastName())
                .phone(c.getPhone())
                .status(DtoMapper.customerStatusForResponse(c.getStatus()))
                .addresses(addresses)
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
