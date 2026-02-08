package com.caixabanktech.loan.infrastructure.adapter.input.rest.error;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Custom structure for API errors")
public record ApiErrorResponse(
    @Schema(description = "Short error title", example = "Business Rule Violation")
    String title,

    @Schema(description = "HTTP Status code", example = "400")
    int status,

    @Schema(description = "Detailed error message", example = "The loan amount must be positive")
    String detail,

    @Schema(description = "Timestamp when the error occurred")
    LocalDateTime timestamp,

    @Schema(
            description = "Optional map of field-level validation errors",
            example = "{\"applicantName\": \"must not be null\", \"loanAmount\": \"must be greater than 0\"}"
    )
    Map<String, String> validationErrors
) {}

