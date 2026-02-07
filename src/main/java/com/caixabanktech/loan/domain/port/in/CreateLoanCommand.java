package com.caixabanktech.loan.domain.port.in;

import java.math.BigDecimal;

public record CreateLoanCommand(String applicantName, BigDecimal amount, String currency, String applicantIdentity) {

}
