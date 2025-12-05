package com.training.demo.dto.request.Auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class RefreshTokenRequest {

    @NotBlank(message = "Token not blank")
    private String refreshToken;
}
