package com.training.demo.dto.request.Otp;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class SendOtpRequest {
    @NotBlank(message = "Email not null")
    private String email;
}
