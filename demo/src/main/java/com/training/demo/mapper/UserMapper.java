package com.training.demo.mapper;

import com.training.demo.dto.response.User.UserDetailsResponse;
import com.training.demo.dto.response.User.UserResponse;
import com.training.demo.entity.User;

public class UserMapper {

    public static UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .status(user.getStatus())
                .verifyEmail(user.isVerifyEmail())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    public static UserDetailsResponse toUserDetailsResponse(User user) {
        return UserDetailsResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .email(user.getEmail())
                .status(user.getStatus())
                .verifyEmail(user.isVerifyEmail())
                .avatarUrl(user.getAvatarUrl())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName().name()) // ví dụ ADMIN, USER
                        .toList())
                .build();
    }

}
