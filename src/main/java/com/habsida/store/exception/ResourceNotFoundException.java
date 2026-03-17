package com.habsida.store.exception;

/**
 * Thrown when a requested resource (entity) is not found.
 * Handled by {@link GlobalExceptionHandler} with 404 and consistent error body.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Object id) {
        super(resourceName + " not found with id: " + id);
    }
}
