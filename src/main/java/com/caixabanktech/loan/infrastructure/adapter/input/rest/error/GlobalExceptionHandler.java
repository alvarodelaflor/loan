package com.caixabanktech.loan.infrastructure.adapter.input.rest.error;

import com.caixabanktech.loan.domain.exception.InvalidDomainDataException;
import com.caixabanktech.loan.domain.exception.InvalidStateTransitionException;
import com.caixabanktech.loan.domain.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles business logic violations (400 Bad Request).
     * RFC 7807 compliant
     */
    @ExceptionHandler({
            InvalidDomainDataException.class,
            InvalidStateTransitionException.class,
            IllegalArgumentException.class
    })
    public ProblemDetail handleBusinessExceptions(RuntimeException e) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        pd.setTitle("Business Rule Violation");
        pd.setType(URI.create("https://api.caixabank.com/errors/bad-request"));
        return pd;
    }

    /**
     * Handles missing resources (404 Not Found).
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException e) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        pd.setTitle("Resource Not Found");
        return pd;
    }

    /**
     * Handles unexpected system failures (500 Internal Server Error).
     * Prevents stack trace leakage.
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGlobalException(Exception e) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred"
        );
        pd.setTitle("Internal Server Error");
        return pd;
    }
}