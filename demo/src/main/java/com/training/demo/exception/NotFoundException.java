package com.training.demo.exception;

import com.training.demo.utils.constants.ApiConstants;
import org.springframework.http.HttpStatus;

public class NotFoundException extends DomainException {
    
    public NotFoundException(String message) {
        super(message, ApiConstants.ErrorCodes.NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    public NotFoundException(String message, Object details) {
        super(message, ApiConstants.ErrorCodes.NOT_FOUND, HttpStatus.NOT_FOUND, details);
    }
}
