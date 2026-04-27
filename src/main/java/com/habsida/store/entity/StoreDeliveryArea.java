package com.habsida.store.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "store_delivery_areas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreDeliveryArea extends BaseEntity {

    @Column(name = "store_id")
    private Long storeId;

    private String name;

    @Column(precision = 19, scale = 4)
    private java.math.BigDecimal fee;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", insertable = false, updatable = false)
    private Store store;
}
