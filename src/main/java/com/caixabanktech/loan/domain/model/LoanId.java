package com.caixabanktech.loan.domain.model;

import java.util.UUID;

public record LoanId(UUID value) {
    public LoanId {
        if (value == null) throw new IllegalArgumentException("LoanId cannot be null");
    }
}
