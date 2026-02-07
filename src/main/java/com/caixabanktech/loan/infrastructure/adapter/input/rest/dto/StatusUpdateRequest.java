package com.caixabanktech.loan.infrastructure.adapter.input.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record StatusUpdateRequest(
        @Schema(
                description = "The target status for the transition",
                allowableValues = {"APPROVED", "REJECTED", "CANCELLED"},
                example = "APPROVED"
        )
        @NotBlank String status
) {}