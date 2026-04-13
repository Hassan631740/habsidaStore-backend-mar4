package com.habsida.store.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_item_modifiers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemModifier extends BaseEntity {

    @Column(name = "order_item_id")
    private Long orderItemId;

    @Column(name = "modifier_option_id")
    private Long modifierOptionId;

    @Column(name = "option_name_snapshot", length = 500)
    private String optionNameSnapshot;

    @Column(precision = 19, scale = 4)
    private BigDecimal price;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", insertable = false, updatable = false)
    private OrderItem orderItem;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modifier_option_id", insertable = false, updatable = false)
    private ModifierOption modifierOption;
}
