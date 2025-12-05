package com.training.demo.exception;

import com.training.demo.utils.constants.ApiConstants;
import org.springframework.http.HttpStatus;

public class TokenException extends DomainException {
    
    public TokenException(String message) {
        super(message, ApiConstants.ErrorCodes.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
    }
    
    public TokenException(String message, Object details) {
        super(message, ApiConstants.ErrorCodes.TOKEN_INVALID, HttpStatus.UNAUTHORIZED, details);
    }
}
