package com.training.demo.mapper;

import com.training.demo.dto.response.Auth.AuthResponse;

public class AuthMapper {

    public static AuthResponse toResponse(String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
