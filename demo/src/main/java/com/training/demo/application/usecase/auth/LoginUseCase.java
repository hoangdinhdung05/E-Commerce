package com.training.demo.application.usecase.auth;

import com.training.demo.dto.request.Auth.LoginRequest;
import com.training.demo.dto.response.Auth.LoginResponse;
import com.training.demo.entity.User;
import com.training.demo.exception.BadRequestException;
import com.training.demo.exception.NotFoundException;
import com.training.demo.repository.UserRepository;
import com.training.demo.security.JwtProvider;
import com.training.demo.utils.constants.ApiConstants;
import com.training.demo.utils.constants.SecurityConstants;
import com.training.demo.utils.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * Use Case: User Login
 * Business logic: Authenticate user and generate JWT tokens
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LoginUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional(readOnly = true)
    public LoginResponse execute(LoginRequest request) {
        log.info("[LoginUseCase] Authenticating user: {}", request.getUsername());

        // 1. Find user by username
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new NotFoundException(ApiConstants.Messages.USER_NOT_FOUND));

        // 2. Validate user status
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new BadRequestException(ApiConstants.Messages.USER_INACTIVE);
        }
        if (user.getStatus() == UserStatus.BANNED) {
            throw new BadRequestException(ApiConstants.Messages.USER_BANNED);
        }

        // 3. Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException(ApiConstants.Messages.INVALID_CREDENTIALS);
        }

        // 4. Check email verification
        if (!user.isVerifyEmail()) {
            throw new BadRequestException(ApiConstants.Messages.EMAIL_NOT_VERIFIED);
        }

        // 5. Generate tokens
        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshToken = jwtProvider.generateRefreshToken(user);

        // 6. Store tokens in Redis
        storeTokensInRedis(user.getId(), accessToken, refreshToken);

        log.info("[LoginUseCase] User logged in successfully: {}", user.getUsername());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles())
                .build();
    }

    private void storeTokensInRedis(Long userId, String accessToken, String refreshToken) {
        String accessTokenKey = SecurityConstants.RedisKeys.ACCESS_TOKEN_PREFIX + userId;
        String refreshTokenKey = SecurityConstants.RedisKeys.REFRESH_TOKEN_PREFIX + userId;

        redisTemplate.opsForValue().set(
                accessTokenKey, 
                accessToken, 
                SecurityConstants.JWT.ACCESS_TOKEN_EXPIRY_MINUTES, 
                TimeUnit.MINUTES
        );
        
        redisTemplate.opsForValue().set(
                refreshTokenKey, 
                refreshToken, 
                SecurityConstants.JWT.REFRESH_TOKEN_EXPIRY_DAYS, 
                TimeUnit.DAYS
        );
    }
}
