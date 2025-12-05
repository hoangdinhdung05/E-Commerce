package com.training.demo.exception;

import com.training.demo.utils.constants.ApiConstants;
import org.springframework.http.HttpStatus;

public class BadRequestException extends DomainException {

    public BadRequestException(String message) {
        super(message, ApiConstants.ErrorCodes.VALIDATION_ERROR, HttpStatus.BAD_REQUEST);
    }

    public BadRequestException(String message, Object details) {
        super(message, ApiConstants.ErrorCodes.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, details);
    }
}
