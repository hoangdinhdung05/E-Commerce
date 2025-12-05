package com.training.demo.dto.response.User;

import com.training.demo.utils.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private UserStatus status;
    private boolean verifyEmail;
    private String avatarUrl;
}
