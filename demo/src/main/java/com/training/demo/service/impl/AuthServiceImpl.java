package com.training.demo.service.impl;

import com.training.demo.application.usecase.auth.LoginUseCase;
import com.training.demo.application.usecase.auth.RegisterUseCase;
import com.training.demo.dto.request.Auth.LoginRequest;
import com.training.demo.dto.request.Auth.LogoutRequest;
import com.training.demo.dto.request.Auth.RegisterRequest;
import com.training.demo.dto.request.Auth.EmailOtpRequest;
import com.training.demo.dto.request.Otp.SendOtpRequest;
import com.training.demo.dto.response.Auth.AuthResponse;
import com.training.demo.dto.response.Auth.LoginResponse;
import com.training.demo.entity.User;
import com.training.demo.exception.BadRequestException;
import com.training.demo.exception.NotFoundException;
import com.training.demo.exception.TokenException;
import com.training.demo.repository.UserRepository;
import com.training.demo.security.JwtProvider;
import com.training.demo.service.AuthService;
import com.training.demo.service.OtpService;
import com.training.demo.service.RedisService;
import com.training.demo.utils.enums.OtpType;
import com.training.demo.utils.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;
import static com.training.demo.mapper.AuthMapper.toResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RedisService redisService;
    private final OtpService otpService;
    
    // Use Cases
    private final LoginUseCase loginUseCase;
    private final RegisterUseCase registerUseCase;

    /**
     * Authenticate user using Login Use Case
     * Delegates business logic to Use Case layer
     *
     * @param request Username/Password
     * @return AccessToken/RefreshToken
     */
    @Override
    public AuthResponse authenticate(LoginRequest request) {
        log.info("[AuthService] Authenticating user: {}", request.getUsername());
        
        // Delegate to Use Case
        LoginResponse loginResponse = loginUseCase.execute(request);
        
        return toResponse(loginResponse);
    }

    /**
     * Register new user account using Register Use Case
     * Delegates business logic to Use Case layer
     *
     * @param request Basic account information
     */
    @Override
    public void register(RegisterRequest request) {
        log.info("[AuthService] Registering new user: {}", request.getUsername());
        
        // Delegate to Use Case
        User user = registerUseCase.execute(request);
        
        // Send OTP for email verification
        log.info("[AuthService] Sending verification email to: {}", user.getEmail());
        try {
            otpService.sendOtp(SendOtpRequest.builder()
                    .email(user.getEmail())
                    .build(), OtpType.VERIFY_EMAIL);
            log.info("[AuthService] Verification email sent successfully");
        } catch (Exception e) {
            log.error("[AuthService] Error sending verification email: {}", e.getMessage(), e);
            throw new BadRequestException("Could not send verification email");
        }
    }

    /**
     * User gửi mã OTP để active account sau khi Register
     *
     * @param request Email và OTP
     */
    @Override
    public void active(EmailOtpRequest request) {
        log.info("[AuthService] Active new account running");
        otpService.verifyEmail(request);
    }

    /**
     * Sử dụng để get một access và một refresh token mới
     *
     * @param refreshToken refreshToken
     * @return Access/Refresh
     */
    @Override
    public AuthResponse refreshToken(String refreshToken) {
        log.info("[AuthService] Refresh token");

        if (!jwtProvider.validateToken(refreshToken, false)) {
            throw new TokenException("Invalid refresh token");
        }

        String username = jwtProvider.getUsernameFromToken(refreshToken, false);
        String refreshKey = "refresh:" + username;
        Optional<String> storedTokenOpt = redisService.get(refreshKey, String.class);

        if (storedTokenOpt.isEmpty() || !storedTokenOpt.get().equals(refreshToken)) {
            throw new TokenException("Invalid or expired refresh token");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        redisService.delete(refreshKey);

        return generateAndStoreTokens(user);
    }


    /**
     * Đăng xuất khỏi hệ thổng | Remove
     *
     * @param request Access|Refresh
     */
    @Override
    public void logout(LogoutRequest request) {
        log.info("[AuthService] Logout account");

        String username = jwtProvider.getUsernameFromToken(request.getAccessToken(), true);
        redisService.delete("access:" + username);
        redisService.delete("refresh:" + username);
    }

    //========= PRIVATE METHOD =========//

    /**
     * Sinh access và refresh token. Lưu vào redis
     * @param user User đã authen
     * @return Trả về access và refresh token
     */
    private AuthResponse generateAndStoreTokens(User user) {
        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshToken = jwtProvider.generateRefreshToken(user.getUsername());

        String accessKey = "access:" + user.getUsername();
        String refreshKey = "refresh:" + user.getUsername();

        long accessTtl = (jwtProvider.getAccessTokenExpiryDate().getTime() - System.currentTimeMillis()) / 1000;
        long refreshTtl = (jwtProvider.getRefreshTokenExpiryDate().getTime() - System.currentTimeMillis()) / 1000;

        redisService.set(accessKey, accessToken, accessTtl, TimeUnit.SECONDS);
        redisService.set(refreshKey, refreshToken, refreshTtl, TimeUnit.SECONDS);

        return toResponse(accessToken, refreshToken);
    }
}
