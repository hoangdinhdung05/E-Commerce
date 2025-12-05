package com.training.demo.dto.response.Auth;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
}
