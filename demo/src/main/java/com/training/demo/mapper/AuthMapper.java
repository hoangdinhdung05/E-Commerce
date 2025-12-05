package com.training.demo.mapper;

import com.training.demo.dto.response.Auth.AuthResponse;
import com.training.demo.dto.response.Auth.LoginResponse;

public class AuthMapper {

    public static AuthResponse toResponse(String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
    
    public static AuthResponse toResponse(LoginResponse loginResponse) {
        return AuthResponse.builder()
                .accessToken(loginResponse.getAccessToken())
                .refreshToken(loginResponse.getRefreshToken())
                .build();
    }
}
