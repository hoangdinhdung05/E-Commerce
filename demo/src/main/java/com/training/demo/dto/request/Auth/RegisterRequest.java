package com.training.demo.dto.request.Auth;

import com.training.demo.validator.ValidEmail;
import com.training.demo.validator.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class RegisterRequest {

    @NotBlank(message = "Username not null")
    @Size(min = 6, max = 20, message = "The length of the username must be greater than or equal to 6 and less than or equal to 20")
    private String username;

    @NotBlank(message = "Email not null")
    @ValidEmail
    private String email;

    @NotBlank(message = "Password not null")
    @ValidPassword
    private String password;
}
