package com.caixabanktech.loan.infrastructure.adapter.input.rest.error;

import com.caixabanktech.loan.domain.exception.InvalidDomainDataException;
import com.caixabanktech.loan.domain.exception.InvalidStateTransitionException;
import com.caixabanktech.loan.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("REST GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("handleBusinessExceptions should return 400 for InvalidDomainDataException")
    void shouldReturn400ForInvalidDomainData() {
        InvalidDomainDataException ex = new InvalidDomainDataException("bad data");
        ResponseEntity<ApiErrorResponse> response = handler.handleBusinessExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Business Rule Violation", response.getBody().title());
        assertEquals("bad data", response.getBody().detail());
        assertEquals(400, response.getBody().status());
    }

    @Test
    @DisplayName("handleBusinessExceptions should return 400 for InvalidStateTransitionException")
    void shouldReturn400ForInvalidStateTransition() {
        InvalidStateTransitionException ex = new InvalidStateTransitionException("invalid state");
        ResponseEntity<ApiErrorResponse> response = handler.handleBusinessExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("invalid state", response.getBody().detail());
    }

    @Test
    @DisplayName("handleBusinessExceptions should return 400 for IllegalArgumentException")
    void shouldReturn400ForIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("illegal arg");
        ResponseEntity<ApiErrorResponse> response = handler.handleBusinessExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("illegal arg", response.getBody().detail());
    }

    @Test
    @DisplayName("handleNotFound should return 404 with title")
    void shouldReturn404ForResourceNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("missing");
        ResponseEntity<ApiErrorResponse> response = handler.handleNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Resource Not Found", response.getBody().title());
        assertEquals("missing", response.getBody().detail());
    }

    @Test
    @DisplayName("handleGlobalException should return 500 without leaking details")
    void shouldReturn500ForUnexpectedException() {
        Exception ex = new Exception("sensitive message");
        ResponseEntity<ApiErrorResponse> response = handler.handleGlobalException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Internal Server Error", response.getBody().title());
        assertEquals("An unexpected error occurred", response.getBody().detail());
    }

    @Test
    @DisplayName("handleMethodArgumentNotValid should return 400 with validation errors map")
    void shouldReturn400ForValidationFailures() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("createLoanRequest", "applicantName", "must not be null");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<Object> response = handler.handleMethodArgumentNotValid(
                ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, mock(WebRequest.class));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse body = (ApiErrorResponse) response.getBody();
        assertNotNull(body);
        assertEquals("Validation Failed", body.title());
        assertNotNull(body.validationErrors());
        assertEquals("must not be null", body.validationErrors().get("applicantName"));
    }
}
