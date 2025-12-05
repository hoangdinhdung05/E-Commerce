package com.training.demo.dto.response.User;

import com.training.demo.utils.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class UserDetailsResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private UserStatus status;
    private boolean verifyEmail;
    private String avatarUrl;
    private List<String> roles;
    private LocalDateTime createdAt;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
