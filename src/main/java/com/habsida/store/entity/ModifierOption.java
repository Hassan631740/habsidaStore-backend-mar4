package com.habsida.store.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "modifier_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModifierOption extends BaseAuditedEntity {

    @Column(name = "modifier_group_id")
    private Long modifierGroupId;

    private String name;

    @Column(name = "price_adjustment", precision = 19, scale = 4)
    private BigDecimal priceAdjustment;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modifier_group_id", insertable = false, updatable = false)
    private ModifierGroup modifierGroup;
}
