package com.training.demo.dto.request.Auth;

import lombok.Getter;

@Getter
public class LogoutRequest {
    private String accessToken;
    private String refreshToken;
}
