package com.training.demo.dto.request.Otp;

import com.training.demo.utils.enums.OtpType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResendOtpRequest {
    @NotBlank(message = "Email not null")
    private String email;

    @NotNull(message = "Type not null")
    private OtpType type;

    public ResendOtpRequest(String email, OtpType type) {
        this.email = email;
        this.type = type;
    }
}
