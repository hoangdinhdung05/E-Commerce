package com.training.demo.application.usecase.user;

import com.training.demo.dto.response.User.UserResponse;
import com.training.demo.entity.User;
import com.training.demo.exception.NotFoundException;
import com.training.demo.mapper.UserMapper;
import com.training.demo.repository.UserRepository;
import com.training.demo.utils.constants.ApiConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Get User by ID
 * Business logic: Retrieve user with roles and map to response
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GetUserUseCase {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserResponse execute(Long userId) {
        log.info("[GetUserUseCase] Getting user by id: {}", userId);

        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new NotFoundException(
                    String.format(ApiConstants.Messages.USER_NOT_FOUND_ID, userId)
                ));

        return UserMapper.toUserResponse(user);
    }
}
