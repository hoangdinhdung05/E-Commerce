package com.training.demo.controller;

import com.training.demo.dto.request.Otp.ResendOtpRequest;
import com.training.demo.dto.response.System.BaseResponse;
import com.training.demo.service.OtpService;
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
@RequestMapping("/api/otp")
@RequiredArgsConstructor
@Validated
@Slf4j
public class OtpController {

    private final OtpService otpService;

    /**
     * Resend OTP with rate limiting protection.
     *
     * @param request request containing email and type
     * @return HTTP 200 if resend successful
     */
    @PostMapping("/resend")
    public ResponseEntity<?> resendOtp(@RequestBody @Valid ResendOtpRequest request) {
        log.info("[OTP] Sending OTP to email: {}", request.getEmail());
        otpService.resendOtp(request);
        return ResponseEntity.ok(BaseResponse.success());
    }
}