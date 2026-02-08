package com.caixabanktech.loan.domain.model;

import com.caixabanktech.loan.domain.exception.InvalidStateTransitionException;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@Getter
@Builder
@Jacksonized
public class LoanApplication {
    @NonNull
    private final LoanId id;
    @NonNull
    private final String applicantName;
    @NonNull
    private final ApplicantIdentity applicantIdentity;
    @NonNull
    private final LoanAmount loanAmount;
    @NonNull
    private final Instant createdAt;
    @NonNull
    private Instant modifiedAt;
    @NonNull
    private LoanStatus status;

    public LoanApplication approve() {
        if (this.status!= LoanStatus.PENDING) throw new InvalidStateTransitionException("Only PENDING -> APPROVED");
        this.status = LoanStatus.APPROVED;
        return this;
    }

    public LoanApplication reject() {
        if (this.status!= LoanStatus.PENDING) throw new InvalidStateTransitionException("Only PENDING -> REJECTED");
        this.status = LoanStatus.REJECTED;
        return this;
    }

    public LoanApplication cancel() {
        if (this.status!= LoanStatus.APPROVED) throw new InvalidStateTransitionException("Only APPROVED -> CANCELLED");
        this.status = LoanStatus.CANCELLED;
        return this;
    }
}