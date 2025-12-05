package com.training.demo.dto.request.User;

import com.training.demo.utils.enums.UserStatus;
import lombok.Getter;

@Getter
public class ChangeStatusRequest {
    private UserStatus status;
}
