package com.caixabanktech.loan.domain.port.in;

import com.caixabanktech.loan.domain.model.LoanApplication;

import java.util.UUID;

public interface ModifyLoanStatusUseCase {
    LoanApplication approveLoan(UUID id);
    LoanApplication rejectLoan(UUID id);
    LoanApplication cancelLoan(UUID id);
}
