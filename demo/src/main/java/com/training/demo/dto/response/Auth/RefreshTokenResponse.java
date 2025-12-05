package com.training.demo.dto.response.Auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RefreshTokenResponse {
    private String accessToken;
    private String refreshToken;
}
