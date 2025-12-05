package com.training.demo.utils.constants;

/**
 * Validation-related constants
 */
public final class ValidationConstants {
    
    private ValidationConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    /**
     * Pagination
     */
    public static final class Pagination {
        private Pagination() {}
        
        public static final int DEFAULT_PAGE_NUMBER = 0;
        public static final int DEFAULT_PAGE_SIZE = 10;
        public static final int MIN_PAGE_SIZE = 1;
        public static final int MAX_PAGE_SIZE = 100;
    }
    
    /**
     * User Validation
     */
    public static final class User {
        private User() {}
        
        public static final int USERNAME_MIN_LENGTH = 3;
        public static final int USERNAME_MAX_LENGTH = 50;
        public static final int EMAIL_MAX_LENGTH = 100;
        public static final int FIRSTNAME_MAX_LENGTH = 50;
        public static final int LASTNAME_MAX_LENGTH = 50;
    }
    
    /**
     * Product Validation
     */
    public static final class Product {
        private Product() {}
        
        public static final int NAME_MIN_LENGTH = 3;
        public static final int NAME_MAX_LENGTH = 200;
        public static final int DESCRIPTION_MAX_LENGTH = 1000;
        public static final String PRICE_MIN = "0.01";
        public static final String PRICE_MAX = "999999999.99";
        public static final int QUANTITY_MIN = 0;
        public static final int QUANTITY_MAX = 999999;
    }
    
    /**
     * Order Validation
     */
    public static final class Order {
        private Order() {}
        
        public static final int SHIPPING_ADDRESS_MAX_LENGTH = 500;
        public static final int NOTE_MAX_LENGTH = 500;
    }
    
    /**
     * File Upload
     */
    public static final class FileUpload {
        private FileUpload() {}
        
        public static final long MAX_FILE_SIZE_MB = 5;
        public static final long MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024;
        public static final String[] ALLOWED_IMAGE_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "webp"};
        public static final String[] ALLOWED_IMAGE_MIME_TYPES = {
            "image/jpeg", "image/png", "image/gif", "image/webp"
        };
    }
    
    /**
     * Validation Messages
     */
    public static final class Messages {
        private Messages() {}
        
        public static final String INVALID_PAGE_NUMBER = "Page number must be greater than or equal to 0";
        public static final String INVALID_PAGE_SIZE = "Page size must be between 1 and 100";
        public static final String REQUIRED_FIELD = "This field is required";
        public static final String INVALID_EMAIL = "Invalid email format";
        public static final String INVALID_USERNAME = "Username must be between 3 and 50 characters";
        public static final String INVALID_PASSWORD = "Password must be at least 8 characters";
        public static final String INVALID_PRICE = "Price must be greater than 0";
        public static final String INVALID_QUANTITY = "Quantity must be greater than or equal to 0";
    }
}
