package com.habsida.store.enums;

/**
 * Order lifecycle status. Stored as VARCHAR in DB.
 */
public enum OrderStatus {
    /** New order; awaiting merchant accept/reject. */
    NEW,
    /**
     * Legacy alias for NEW. DB rows may contain this value; the API sets NEW on all new orders.
     * Do not use for new orders. Will be removed once existing PENDING rows are migrated to NEW.
     */
    @Deprecated
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
