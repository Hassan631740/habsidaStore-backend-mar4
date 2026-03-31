package com.habsida.store.entity;

import com.habsida.store.enums.CustomerStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer extends BaseAuditedEntity {

    @Column(name = "user_id")
    private Long userId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private String phone;

    /**
     * {@link com.habsida.store.enums.CustomerStatus} name, e.g. ACTIVE, SUSPENDED.
     */
    @Column(name = "status", length = 32)
    @Builder.Default
    private String status = CustomerStatus.ACTIVE.name();
}
