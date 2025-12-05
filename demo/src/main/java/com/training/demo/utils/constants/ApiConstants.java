package com.training.demo.utils.constants;

/**
 * API-related constants
 */
public final class ApiConstants {
    
    private ApiConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    /**
     * API Response Messages
     */
    public static final class Messages {
        private Messages() {}
        
        // Success messages
        public static final String SUCCESS = "Success";
        public static final String CREATED_SUCCESS = "Created successfully";
        public static final String UPDATED_SUCCESS = "Updated successfully";
        public static final String DELETED_SUCCESS = "Deleted successfully";
        
        // Error messages
        public static final String INTERNAL_SERVER_ERROR = "Internal server error";
        public static final String BAD_REQUEST = "Bad request";
        public static final String UNAUTHORIZED = "Unauthorized";
        public static final String FORBIDDEN = "Forbidden";
        public static final String NOT_FOUND = "Not found";
        public static final String CONFLICT = "Conflict";
        
        // Validation messages
        public static final String VALIDATION_FAILED = "Validation failed";
        public static final String INVALID_INPUT = "Invalid input";
        
        // Auth messages
        public static final String LOGIN_SUCCESS = "Login successful";
        public static final String LOGOUT_SUCCESS = "Logout successful";
        public static final String REGISTER_SUCCESS = "Registration successful";
        public static final String INVALID_CREDENTIALS = "Invalid username or password";
        public static final String ACCOUNT_LOCKED = "Account has been locked";
        public static final String EMAIL_NOT_VERIFIED = "Email not verified";
        public static final String TOKEN_EXPIRED = "Token has expired";
        public static final String TOKEN_INVALID = "Invalid token";
        
        // User messages
        public static final String USER_NOT_FOUND = "User not found";
        public static final String USER_ALREADY_EXISTS = "User already exists";
        public static final String EMAIL_ALREADY_EXISTS = "Email already exists";
        public static final String USERNAME_ALREADY_EXISTS = "Username already exists";
        
        // Product messages
        public static final String PRODUCT_NOT_FOUND = "Product not found";
        public static final String INSUFFICIENT_STOCK = "Insufficient stock";
        
        // Order messages
        public static final String ORDER_NOT_FOUND = "Order not found";
        public static final String INVALID_ORDER_STATUS = "Invalid order status";
        public static final String CANNOT_CANCEL_ORDER = "Cannot cancel order in current status";
        
        // Cart messages
        public static final String CART_NOT_FOUND = "Cart not found";
        public static final String CART_EMPTY = "Cart is empty";
        
        // Payment messages
        public static final String PAYMENT_FAILED = "Payment failed";
        public static final String PAYMENT_SUCCESS = "Payment successful";
    }
    
    /**
     * Error Codes
     */
    public static final class ErrorCodes {
        private ErrorCodes() {}
        
        // General
        public static final String INTERNAL_ERROR = "ERR_INTERNAL";
        public static final String VALIDATION_ERROR = "ERR_VALIDATION";
        public static final String NOT_FOUND = "ERR_NOT_FOUND";
        public static final String CONFLICT = "ERR_CONFLICT";
        
        // Auth
        public static final String INVALID_CREDENTIALS = "ERR_AUTH_001";
        public static final String TOKEN_EXPIRED = "ERR_AUTH_002";
        public static final String TOKEN_INVALID = "ERR_AUTH_003";
        public static final String UNAUTHORIZED = "ERR_AUTH_004";
        public static final String ACCOUNT_LOCKED = "ERR_AUTH_005";
        public static final String EMAIL_NOT_VERIFIED = "ERR_AUTH_006";
        
        // User
        public static final String USER_NOT_FOUND = "ERR_USER_001";
        public static final String USER_ALREADY_EXISTS = "ERR_USER_002";
        public static final String EMAIL_ALREADY_EXISTS = "ERR_USER_003";
        public static final String USERNAME_ALREADY_EXISTS = "ERR_USER_004";
        
        // Product
        public static final String PRODUCT_NOT_FOUND = "ERR_PRODUCT_001";
        public static final String INSUFFICIENT_STOCK = "ERR_PRODUCT_002";
        
        // Order
        public static final String ORDER_NOT_FOUND = "ERR_ORDER_001";
        public static final String INVALID_ORDER_STATUS = "ERR_ORDER_002";
        public static final String CANNOT_CANCEL_ORDER = "ERR_ORDER_003";
        
        // Payment
        public static final String PAYMENT_FAILED = "ERR_PAYMENT_001";
    }
    
    /**
     * HTTP Status Messages
     */
    public static final class HttpStatus {
        private HttpStatus() {}
        
        public static final int OK = 200;
        public static final int CREATED = 201;
        public static final int NO_CONTENT = 204;
        public static final int BAD_REQUEST = 400;
        public static final int UNAUTHORIZED = 401;
        public static final int FORBIDDEN = 403;
        public static final int NOT_FOUND = 404;
        public static final int CONFLICT = 409;
        public static final int INTERNAL_SERVER_ERROR = 500;
    }
}
