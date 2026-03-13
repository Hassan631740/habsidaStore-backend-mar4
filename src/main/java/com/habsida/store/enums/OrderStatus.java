package com.habsida.store.enums;

/**
 * Order lifecycle status. Stored as VARCHAR in DB.
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    READY,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
