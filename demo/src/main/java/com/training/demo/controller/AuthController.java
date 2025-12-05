package com.training.demo.controller;

import com.training.demo.dto.request.Auth.*;
import com.training.demo.dto.response.System.BaseResponse;
import com.training.demo.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AuthController {

    private final AuthService authService;

    /**
     * Sử lí authenticate user
     * @param request Username/Password
     * @return AccessToken/RefreshToken
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request) {
        log.info("[AUTH] API login with username: {}", request.getUsername());
        return ResponseEntity.ok(BaseResponse
                .success(authService.authenticate(request)));
    }

    /**
     * Đăng kí account
     * @param request Info user
     * @return Success
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest request) {
        log.info("[AUTH] API register new account with username: {}", request.getUsername());
        authService.register(request);
        return ResponseEntity.ok(BaseResponse.success());
    }

    /**
     * Active account sau khi register
     * @param request email và otp
     * @return Success
     */
    @PostMapping("/active")
    public ResponseEntity<?> active(@RequestBody @Valid EmailOtpRequest request) {
        log.info("[AUTH] API active new account with email: {}", request.getEmail());
        authService.active(request);
        return ResponseEntity.ok(BaseResponse.success());
    }

    /**
     * Sinh access và refresh token mới
     * @param request RefreshToken
     * @return ACCESS|REFRESH
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refresh(@RequestBody @Valid RefreshTokenRequest request) {
        log.info("[AUTH] API refresh token");
        return ResponseEntity.ok(BaseResponse.success(authService.refreshToken(request.getRefreshToken())));
    }

    /**
     * Logout account
     * @param request access|refresh
     * @return void
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody @Valid LogoutRequest request) {
        log.info("[AUTH] API logout");
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }
}
