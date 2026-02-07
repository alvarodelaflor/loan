package com.caixabanktech.loan.infrastructure.adapter.input.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

public record LoanResponse(
        @Schema(example = "c18b4e1b-6b10-4d6c-9476-5e4764facb30", description = "Unique identifier of the loan application")
        String id,
        @Schema(example = "Alvaro de la Flor Bonilla", description = "Full name of the applicant")
        String applicantName,
        @Schema(example = "12345678Z", description = "Spanish National Identity Document (DNI or NIE)")
        String applicantIdentity,
        @Schema(example = "25000.50", description = "Total requested amount")
        BigDecimal loanAmount,
        @Schema(example = "EUR", description = "Three-letter ISO currency code")
        String currency,
        @Schema(example = "2024-06-01T12:00:00Z", description = "Timestamp when the loan application was created")
        Instant createdAt,
        @Schema(example = "2024-06-02T15:30:00Z", description = "Timestamp when the loan application was last modified")
        Instant modifiedAt,
        @Schema(example = "PENDING", description = "Current status of the loan application")
        String status) {

}
