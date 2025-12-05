package com.training.demo.service.impl;

import com.training.demo.dto.request.Auth.LoginRequest;
import com.training.demo.dto.request.Auth.LogoutRequest;
import com.training.demo.dto.request.Auth.RegisterRequest;
import com.training.demo.dto.request.Auth.EmailOtpRequest;
import com.training.demo.dto.request.Otp.SendOtpRequest;
import com.training.demo.dto.response.Auth.AuthResponse;
import com.training.demo.entity.User;
import com.training.demo.exception.BadRequestException;
import com.training.demo.exception.NotFoundException;
import com.training.demo.exception.TokenException;
import com.training.demo.repository.UserRepository;
import com.training.demo.security.CustomUserDetails;
import com.training.demo.security.JwtProvider;
import com.training.demo.service.AuthService;
import com.training.demo.service.OtpService;
import com.training.demo.service.RedisService;
import com.training.demo.service.UserService;
import com.training.demo.utils.enums.OtpType;
import com.training.demo.utils.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import static com.training.demo.mapper.AuthMapper.toResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final UserService userService;
    private final RedisService redisService;
    private final OtpService otpService;

    /**
     * Sử dụng cơ chế Spring Security để tiến hành Authenticate User
     * Sinh accessToken và refreshToken hỗ trợ trong quá trình sử dụng
     *
     * @param request Username(Email)/Password
     * @return AccessToken/RefreshToken
     */
    @Override
    public AuthResponse authenticate(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            if (!user.isVerifyEmail() || user.getStatus().equals(UserStatus.INACTIVE)) {
                throw new BadRequestException("Account is not activated or has been locked");
            }

            return generateAndStoreTokens(user);
        } catch (AuthenticationException e) {
            throw new BadRequestException("Username or password incorrect");
        }
    }

    /**
     * Đăng kí tài khoản mới (Gán role mặc định cho account là RoleType.User)
     *
     * @param request Thông tin cở bản của Account
     */
    @Override
    public void register(RegisterRequest request) {
        log.info("[AuthService] Register new account with username: {}", request.getUsername());
        userService.register(request);
        log.info("SendMail with OTP to username: {}", request.getUsername());

        try {
            otpService.sendOtp(SendOtpRequest.builder()
                            .email(request.getEmail())
                    .build(), OtpType.VERIFY_EMAIL);
            log.info("Sendmail success");
        } catch (Exception e) {
            log.error("Sendmail register error: {}", e.getMessage(), e);
            throw new BadRequestException("Sendmail register error");
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
