package com.caixabanktech.loan.infrastructure.adapter.input.rest.error;

import com.caixabanktech.loan.domain.exception.InvalidDomainDataException;
import com.caixabanktech.loan.domain.exception.InvalidStateTransitionException;
import com.caixabanktech.loan.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("REST GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("handleBusinessExceptions should return 400 with RFC7807 details for InvalidDomainDataException")
    void handleBusinessExceptions_InvalidDomainData() {
        InvalidDomainDataException ex = new InvalidDomainDataException("bad data");
        ProblemDetail pd = handler.handleBusinessExceptions(ex);
        assertEquals(HttpStatus.BAD_REQUEST.value(), pd.getStatus());
        assertEquals("Business Rule Violation", pd.getTitle());
        assertEquals("bad data", pd.getDetail());
        assertEquals(URI.create("https://api.caixabank.com/errors/bad-request"), pd.getType());
    }

    @Test
    @DisplayName("handleBusinessExceptions should return 400 for InvalidStateTransitionException")
    void handleBusinessExceptions_InvalidStateTransition() {
        InvalidStateTransitionException ex = new InvalidStateTransitionException("invalid state");
        ProblemDetail pd = handler.handleBusinessExceptions(ex);
        assertEquals(HttpStatus.BAD_REQUEST.value(), pd.getStatus());
        assertEquals("invalid state", pd.getDetail());
    }

    @Test
    @DisplayName("handleBusinessExceptions should return 400 for IllegalArgumentException")
    void handleBusinessExceptions_IllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("illegal arg");
        ProblemDetail pd = handler.handleBusinessExceptions(ex);
        assertEquals(HttpStatus.BAD_REQUEST.value(), pd.getStatus());
        assertEquals("illegal arg", pd.getDetail());
    }

    @Test
    @DisplayName("handleNotFound should return 404 with title")
    void handleNotFound_Returns404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("missing");
        ProblemDetail pd = handler.handleNotFound(ex);
        assertEquals(HttpStatus.NOT_FOUND.value(), pd.getStatus());
        assertEquals("Resource Not Found", pd.getTitle());
        assertEquals("missing", pd.getDetail());
    }

    @Test
    @DisplayName("handleGlobalException should return 500 without leaking details")
    void handleGlobalException_Returns500() {
        Exception ex = new Exception("sensitive message");
        ProblemDetail pd = handler.handleGlobalException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), pd.getStatus());
        assertEquals("Internal Server Error", pd.getTitle());
        assertEquals("An unexpected error occurred", pd.getDetail());
    }
}
