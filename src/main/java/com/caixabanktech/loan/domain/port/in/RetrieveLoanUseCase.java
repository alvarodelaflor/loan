package com.caixabanktech.loan.domain.port.in;

import com.caixabanktech.loan.domain.model.ApplicantIdentity;
import com.caixabanktech.loan.domain.model.LoanApplication;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface RetrieveLoanUseCase {
    LoanApplication getLoan(UUID id);
    List<LoanApplication> getLoanHistory(UUID id);
    List<LoanApplication> getLoansByIdentity(String identity);
    List<LoanApplication> searchLoans(String identity, Instant startDate, Instant endDate);
    void deleteLoan(UUID id);
}
