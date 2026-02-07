package com.caixabanktech.loan.domain.exception;

public class InvalidDomainDataException extends RuntimeException {
    public InvalidDomainDataException(String message) {
        super(message);
    }
}
