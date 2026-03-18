package com.habsida.store.enums;

/**
 * Order lifecycle status. Stored as VARCHAR in DB.
 */
public enum OrderStatus {
    /** New order; awaiting merchant accept/reject. */
    NEW,
    /** Placed; awaiting merchant accept/reject (legacy). */
    PENDING,
    /** Merchant rejected the order. */
    REJECTED,
    CONFIRMED,
    PROCESSING,
    READY,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
