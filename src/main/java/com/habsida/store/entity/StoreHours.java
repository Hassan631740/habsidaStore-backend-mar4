package com.habsida.store.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "store_hours")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreHours extends BaseEntity {

    @Column(name = "store_id")
    private Long storeId;

    @Column(name = "day_of_week")
    private Integer dayOfWeek;

    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    /** Orders placed after this time on the same day are deferred to the next opening. */
    @Column(name = "last_order_cutoff_time")
    private LocalTime lastOrderCutoffTime;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", insertable = false, updatable = false)
    private Store store;
}
