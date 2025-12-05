package com.training.demo.dto.request.Auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class EmailOtpRequest {

    @NotBlank(message = "Email not null")
    private String email;

    @NotBlank(message = "OTP code not null")
    private String otp;
}
