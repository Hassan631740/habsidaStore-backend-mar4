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
    /** Merchant rejected the order. */
    REJECTED,
    CONFIRMED,
    PROCESSING,
    READY,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
