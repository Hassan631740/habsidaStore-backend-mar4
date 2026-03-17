package com.habsida.store.enums;

/**
 * Payment status. Stored as VARCHAR in DB.
 */
public enum PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REFUNDED,
    CANCELLED
}
