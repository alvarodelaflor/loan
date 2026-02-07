package com.caixabanktech.loan.infrastructure.adapter.input.rest;

import com.caixabanktech.loan.domain.model.ApplicantIdentity;
import com.caixabanktech.loan.domain.port.in.CreateLoanCommand;
import com.caixabanktech.loan.domain.port.in.CreateLoanUseCase;
import com.caixabanktech.loan.domain.port.in.ModifyLoanStatusUseCase;
import com.caixabanktech.loan.domain.port.in.RetrieveLoanUseCase;
import com.caixabanktech.loan.infrastructure.adapter.input.rest.dto.CreateLoanRequest;
import com.caixabanktech.loan.infrastructure.adapter.input.rest.dto.LoanResponse;
import com.caixabanktech.loan.infrastructure.adapter.input.rest.dto.StatusUpdateRequest;
import com.caixabanktech.loan.infrastructure.adapter.output.persistence.mapper.LoanRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/loans")
@Tag(name = "Loan Management", description = "Endpoints for managing the end-to-end lifecycle of personal loan applications.")
public class LoanController {

    private final CreateLoanUseCase createUseCase;
    private final ModifyLoanStatusUseCase modifyStatusUseCase;
    private final RetrieveLoanUseCase retrieveUseCase;
    private final LoanRestMapper loanRestMapper;

    public LoanController(CreateLoanUseCase createUseCase,
                          ModifyLoanStatusUseCase modifyStatusUseCase,
                          RetrieveLoanUseCase retrieveUseCase,
                          LoanRestMapper loanRestMapper) {
        this.createUseCase = createUseCase;
        this.modifyStatusUseCase = modifyStatusUseCase;
        this.retrieveUseCase = retrieveUseCase;
        this.loanRestMapper = loanRestMapper;
    }

    @Operation(
            summary = "Submit a new loan application",
            description = "Initializes a loan request with 'PENDING' status. Validates identity documents (DNI/NIE) and financial invariants."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Loan application successfully created",
                    content = @Content(schema = @Schema(implementation = LoanResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data (e.g., checksum error in NIF, negative amount)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Internal system failure",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoanResponse> create(@RequestBody @Valid CreateLoanRequest request) {
        var command = new CreateLoanCommand(
                request.applicantName(),
                request.amount(),
                request.currency(),
                request.identityDocument()
        );
        var loan = createUseCase.createLoan(command);
        return new ResponseEntity<>(loanRestMapper.toResponse(loan), HttpStatus.CREATED);
    }

    @Operation(summary = "Retrieve application details", description = "Fetches the current state of a specific loan application by its unique UUID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Application found"),
            @ApiResponse(responseCode = "404", description = "Loan application not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<LoanResponse> get(
            @Parameter(description = "The unique UUID of the loan", example = "c18b4e1b-6b10-4d6c-9476-5e4764facb30")
            @PathVariable UUID id) {
        return ResponseEntity.ok(loanRestMapper.toResponse(retrieveUseCase.getLoan(id)));
    }

    @Operation(summary = "Consult audit history", description = "Returns a complete chronological list of all state changes for the application (powered by Hibernate Envers).")
    @ApiResponse(responseCode = "200", description = "Historical data retrieved",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = LoanResponse.class))))
    @GetMapping("/{id}/history")
    public ResponseEntity<List<LoanResponse>> getHistory(
            @Parameter(description = "The unique UUID of the loan", example = "c18b4e1b-6b10-4d6c-9476-5e4764facb30")
            @PathVariable UUID id) {
        return ResponseEntity.ok(retrieveUseCase.getLoanHistory(id).stream()
                .map(loanRestMapper::toResponse).toList());
    }

    @Operation(
            summary = "Advance application status",
            description = "Triggers a state transition. Permitted flows: PENDING -> APPROVED/REJECTED, APPROVED -> CANCELLED."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Status successfully updated"),
            @ApiResponse(responseCode = "400", description = "Illegal state transition or unknown status value",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Loan application not found")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @Parameter(description = "The unique UUID of the loan", example = "c18b4e1b-6b10-4d6c-9476-5e4764facb30")
            @PathVariable UUID id,
            @RequestBody @Valid StatusUpdateRequest request) {

        switch (request.status()) {
            case "APPROVED" -> modifyStatusUseCase.approveLoan(id);
            case "REJECTED" -> modifyStatusUseCase.rejectLoan(id);
            case "CANCELLED" -> modifyStatusUseCase.cancelLoan(id);
            case "PENDING" -> throw new IllegalArgumentException("Cannot transition back to PENDING status");
            default -> throw new IllegalArgumentException("Unknown status action: " + request.status());
        }
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Search loans by applicant", description = "Retrieves all loan applications associated with a specific DNI/NIE.")
    @ApiResponse(responseCode = "200", description = "Search completed")
    @GetMapping("/search/{applicantIdentity}")
    public ResponseEntity<List<LoanResponse>> searchByIdentity(
            @Parameter(example = "12345678Z", description = "Spanish National Identity Document (DNI or NIE)")
            @PathVariable ApplicantIdentity applicantIdentity) {
        var loans = retrieveUseCase.getLoansByIdentity(applicantIdentity);
        return ResponseEntity.ok(loanRestMapper.toResponseList(loans));
    }

    @Operation(summary = "Search loans with filters", description = "Filters by DNI/NIE and/or creation date range")
    @GetMapping("/search/criteria")
    public ResponseEntity<List<LoanResponse>> search(
            @Parameter(example = "12345678Z", description = "Spanish National Identity Document (DNI or NIE)")
            @RequestParam(required = false) String applicantIdentity,
            @Parameter(example = "2026-02-07T17:51:37Z", description = "Minimum loan creation date")
            @RequestParam(required = false) Instant startDate,
            @Parameter(example = "2026-02-08T17:51:37Z", description = "Maximum loan creation date")
            @RequestParam(required = false) Instant endDate) {

        var results = retrieveUseCase.searchLoans(applicantIdentity, startDate, endDate);
        return ResponseEntity.ok(loanRestMapper.toResponseList(results));
    }

    @Operation(summary = "Delete loan application", description = "Deletes a loan application by its UUID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Loan successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Loan application not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "The unique UUID of the loan")
            @PathVariable UUID id) {
        retrieveUseCase.deleteLoan(id);
        return ResponseEntity.noContent().build();
    }
}