package com.caixabanktech.loan.domain.port.in;

import com.caixabanktech.loan.domain.model.LoanApplication;

public interface CreateLoanUseCase {
    LoanApplication createLoan(CreateLoanCommand command);
}
