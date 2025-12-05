package com.training.demo.dto.request.User;

import com.training.demo.utils.enums.RoleType;
import com.training.demo.utils.enums.UserStatus;
import lombok.Getter;
import java.util.Set;

@Getter
public class UpdateUserRequest {
    private String firstName;
    private String lastName;
    private UserStatus status;
    private boolean verifyEmail;
    private Set<RoleType> roles;
}
