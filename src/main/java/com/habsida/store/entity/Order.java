package com.habsida.store.entity;

import com.habsida.store.enums.OrderStatus;
import com.habsida.store.enums.OrderType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseAuditedEntity {

    @Column(name = "store_id")
    private Long storeId;

    @Column(name = "customer_id")
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private OrderStatus status;

    @Column(name = "order_type")
    @Enumerated(EnumType.STRING)
    private OrderType orderType;

    @Column(name = "total_amount", precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @Column(name = "rejected_at")
    private Instant rejectedAt;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    @Column(length = 2000)
    private String notes;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", insertable = false, updatable = false)
    private Store store;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    private Customer customer;
}