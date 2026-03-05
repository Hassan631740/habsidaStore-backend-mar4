package com.habsida.store.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "modifier_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModifierOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "modifier_group_id")
    private Long modifierGroupId;

    private String name;

    @Column(name = "price_adjustment", precision = 19, scale = 4)
    private BigDecimal priceAdjustment;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modifier_group_id", insertable = false, updatable = false)
    private ModifierGroup modifierGroup;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
