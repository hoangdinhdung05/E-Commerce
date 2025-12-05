package com.training.demo.dto.request.User;

import com.training.demo.validator.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ChangePasswordRequest {
    @NotBlank(message = "Old password not blank")
    private String oldPassword;

    @NotBlank(message = "Old password not blank")
    @ValidPassword
    private String newPassword;

    @NotBlank(message = "Old password not blank")
    private String confirmPassword;
}
