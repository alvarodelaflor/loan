package com.caixabanktech.loan.domain.model;

import com.caixabanktech.loan.domain.exception.InvalidDomainDataException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

public record LoanAmount(BigDecimal amount, Currency currency) {
    public LoanAmount {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidDomainDataException("The amount must be positive");
        }
        if (currency == null) throw new InvalidDomainDataException("Currency is mandatory");
        amount = amount.setScale(2, RoundingMode.HALF_EVEN);
    }
}
