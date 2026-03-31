package com.habsida.store.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

/**
 * Structured API error response for consistent handling across all endpoints.
 * Validation errors (400) include a {@code details} array of { field, message }.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Schema(description = "Error response; validation errors include details[] with field and message")
public class ErrorResponse {

    @Schema(example = "2024-01-15T10:30:00Z")
    private final Instant timestamp;
    @Schema(example = "400")
    private final int status;
    @Schema(example = "Bad Request")
    private final String error;
    @Schema(example = "email: Invalid email format; password: Password is required")
    private final String message;
    @Schema(example = "/api/auth/register")
    private final String path;
    @Schema(description = "Per-field validation errors (present for 400 validation failures)")
    private final List<FieldError> details;

    @Getter
    @Builder
    @Schema(description = "Single field validation error")
    public static class FieldError {
        @Schema(example = "email")
        private final String field;
        @Schema(example = "Invalid email format")
        private final String message;
    }
}
