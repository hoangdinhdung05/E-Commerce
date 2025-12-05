package com.training.demo.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base class for all domain exceptions
 * Provides consistent error handling across the application
 */
@Getter
public abstract class DomainException extends RuntimeException {
    
    private final String errorCode;
    private final HttpStatus httpStatus;
    private final Object details;
    
    protected DomainException(String message, String errorCode, HttpStatus httpStatus) {
        this(message, errorCode, httpStatus, null);
    }
    
    protected DomainException(String message, String errorCode, HttpStatus httpStatus, Object details) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = details;
    }
    
    protected DomainException(String message, Throwable cause, String errorCode, HttpStatus httpStatus) {
        this(message, cause, errorCode, httpStatus, null);
    }
    
    protected DomainException(String message, Throwable cause, String errorCode, HttpStatus httpStatus, Object details) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = details;
    }
}
