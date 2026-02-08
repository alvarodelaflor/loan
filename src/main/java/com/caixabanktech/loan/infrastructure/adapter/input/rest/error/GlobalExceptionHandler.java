package com.caixabanktech.loan.infrastructure.adapter.input.rest.error;

import com.caixabanktech.loan.domain.exception.InvalidDomainDataException;
import com.caixabanktech.loan.domain.exception.InvalidStateTransitionException;
import com.caixabanktech.loan.domain.exception.ResourceNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles business logic violations (400 Bad Request).
     */
    @ExceptionHandler({
            InvalidDomainDataException.class,
            InvalidStateTransitionException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBusinessExceptions(RuntimeException e) {
        var error = new ApiErrorResponse(
                "Business Rule Violation",
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage(),
                LocalDateTime.now(),
                null
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles field validation errors (e.g. @NotNull, @Size).
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        var error = new ApiErrorResponse(
                "Validation Failed",
                status.value(),
                "The provided data is invalid",
                LocalDateTime.now(),
                errors
        );
        return new ResponseEntity<>(error, status);
    }

    /**
     * Handles missing resources (404 Not Found).
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException e) {
        var error = new ApiErrorResponse(
                "Resource Not Found",
                HttpStatus.NOT_FOUND.value(),
                e.getMessage(),
                LocalDateTime.now(),
                null
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles unexpected system failures (500 Internal Server Error).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGlobalException(Exception e) {
        var error = new ApiErrorResponse(
                "Internal Server Error",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred",
                LocalDateTime.now(),
                null
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}