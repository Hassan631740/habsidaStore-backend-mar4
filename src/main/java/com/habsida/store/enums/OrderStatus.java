package com.habsida.store.enums;

/**
 * Order lifecycle status. Stored as VARCHAR in DB.
 */
public enum OrderStatus {
    /** New order; awaiting merchant accept/reject. */
    NEW,
    /** Placed; awaiting merchant accept/reject (legacy). */
    PENDING,
    /** Merchant accepted the order. */
    ACCEPTED,
    /** Merchant started processing (kitchen/prep/etc.). */
    IN_PROGRESS,
    /** Order fulfilled. */
    COMPLETED,
    /** Merchant rejected the order. */
    REJECTED,
    /** Order canceled. */
    CANCELED,

    // ---- Legacy statuses (kept for backward compatibility with existing DB data) ----
    CONFIRMED,
    PROCESSING,
    READY,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
