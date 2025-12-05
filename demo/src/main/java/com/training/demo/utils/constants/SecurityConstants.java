package com.training.demo.utils.constants;

/**
 * Security-related constants
 */
public final class SecurityConstants {
    
    private SecurityConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    /**
     * API Endpoints
     */
    public static final class Endpoints {
        private Endpoints() {}
        
        public static final String API_BASE = "/api";
        
        // Auth endpoints
        public static final String AUTH_BASE = API_BASE + "/auth";
        public static final String AUTH_LOGIN = "/login";
        public static final String AUTH_REGISTER = "/register";
        public static final String AUTH_LOGOUT = "/logout";
        public static final String AUTH_REFRESH = "/refresh-token";
        public static final String AUTH_ACTIVE = "/active";
        
        // Public endpoints array for security config
        public static final String[] PUBLIC_ENDPOINTS = {
            "/",
            AUTH_BASE + AUTH_LOGIN,
            AUTH_BASE + AUTH_REGISTER,
            AUTH_BASE + AUTH_REFRESH,
            AUTH_BASE + AUTH_LOGOUT,
            AUTH_BASE + AUTH_ACTIVE,
            API_BASE + "/otp/**",
            API_BASE + "/products",
            API_BASE + "/products/search/**",
            API_BASE + "/products/search-category/**",
            API_BASE + "/products/count-by-category/**",
            API_BASE + "/categories",
            "/avatars/**",
            "/uploads/**",
            "/products/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**"
        };
    }
    
    /**
     * HTTP Headers
     */
    public static final class Headers {
        private Headers() {}
        
        public static final String AUTHORIZATION = "Authorization";
        public static final String BEARER_PREFIX = "Bearer ";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String CORRELATION_ID = "X-Correlation-ID";
    }
    
    /**
     * Redis Keys
     */
    public static final class RedisKeys {
        private RedisKeys() {}
        
        public static final String ACCESS_TOKEN_PREFIX = "access:";
        public static final String REFRESH_TOKEN_PREFIX = "refresh:";
        public static final String OTP_PREFIX = "otp:";
        public static final String USER_CACHE_PREFIX = "user:";
    }
    
    /**
     * JWT Configuration
     */
    public static final class JWT {
        private JWT() {}
        
        public static final int MIN_TOKEN_LENGTH = 10;
        public static final String ROLES_CLAIM = "roles";
        public static final String USER_ID_CLAIM = "userId";
    }
    
    /**
     * Security Settings
     */
    public static final class Settings {
        private Settings() {}
        
        public static final int PASSWORD_MIN_LENGTH = 8;
        public static final int PASSWORD_MAX_LENGTH = 100;
        public static final int MAX_LOGIN_ATTEMPTS = 5;
        public static final long LOGIN_ATTEMPT_TIMEOUT_MINUTES = 15;
    }
}
