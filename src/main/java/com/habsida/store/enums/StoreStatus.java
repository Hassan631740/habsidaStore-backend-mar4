package com.habsida.store.enums;

/**
 * Store lifecycle status. Stored as VARCHAR in DB.
 */
public enum StoreStatus {
    ACTIVE,
    INACTIVE,
    PENDING,
    CLOSED
}
