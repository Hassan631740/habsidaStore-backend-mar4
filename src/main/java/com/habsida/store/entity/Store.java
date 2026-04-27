package com.habsida.store.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Store extends BaseAuditedEntity {

    private String name;

    /** One of: ACTIVE, INACTIVE, PENDING, CLOSED */
    private String status;

    /** City, district, or free-text location for admin filtering. */
    private String location;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;
}
