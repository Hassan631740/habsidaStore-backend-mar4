package com.habsida.store.enums;

/**
 * Order lifecycle status. Stored as VARCHAR in DB.
 */
public enum OrderStatus {
    /** Placed; awaiting merchant accept/reject. */
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
