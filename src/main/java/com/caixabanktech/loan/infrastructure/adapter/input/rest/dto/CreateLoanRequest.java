package com.caixabanktech.loan.infrastructure.adapter.input.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateLoanRequest(
        @Schema(example = "Alvaro de la Flor Bonilla", description = "Full name of the applicant")
        @NotBlank String applicantName,

        @Schema(example = "25000.50", description = "Total requested amount")
        @NotNull @Positive BigDecimal amount,

        @Schema(example = "EUR", description = "Three-letter ISO currency code")
        @NotBlank @Size(min = 3, max = 3) String currency,

        @Schema(example = "12345678Z", description = "Spanish National Identity Document (DNI or NIE)")
        @NotBlank String identityDocument
) {}
