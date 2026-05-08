package com.habsida.store.controller.me;

import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.response.CustomerAddressResponse;
import com.habsida.store.entity.Customer;
import com.habsida.store.entity.CustomerAddress;
import com.habsida.store.repository.AddressRepository;
import com.habsida.store.repository.CustomerAddressRepository;
import com.habsida.store.repository.CustomerRepository;
import com.habsida.store.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/me/addresses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
@Tag(name = "Customer Addresses", description = "Customer: manage own delivery addresses")
public class MeAddressController {

    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository customerAddressRepository;
    private final AddressRepository addressRepository;

    private Customer resolveCustomer(AuthUser authUser) {
        return customerRepository.findByUserId(authUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No customer profile for this account"));
    }

    @Operation(summary = "List all addresses linked to the authenticated customer")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping
    public List<CustomerAddressResponse> findAll(@AuthenticationPrincipal AuthUser authUser) {
        Customer customer = resolveCustomer(authUser);
        return customerAddressRepository.findByCustomerIdOrderByIdAsc(customer.getId())
                .stream().map(DtoMapper::toResponse).toList();
    }

    @Operation(summary = "Link an existing address to the authenticated customer")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Linked"),
        @ApiResponse(responseCode = "404", description = "Address not found")
    })
    @PostMapping("/{addressId}")
    public ResponseEntity<CustomerAddressResponse> addAddress(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long addressId) {
        Customer customer = resolveCustomer(authUser);
        if (!addressRepository.existsById(addressId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found");
        }
        CustomerAddress link = new CustomerAddress();
        link.setCustomerId(customer.getId());
        link.setAddressId(addressId);
        CustomerAddress saved = customerAddressRepository.save(link);
        return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.toResponse(saved));
    }

    @Operation(summary = "Unlink an address from the authenticated customer")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Unlinked"),
        @ApiResponse(responseCode = "403", description = "Address belongs to a different customer"),
        @ApiResponse(responseCode = "404", description = "Address link not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeAddress(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id) {
        Customer customer = resolveCustomer(authUser);
        CustomerAddress link = customerAddressRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address link not found"));
        if (!customer.getId().equals(link.getCustomerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to remove this address");
        }
        customerAddressRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}