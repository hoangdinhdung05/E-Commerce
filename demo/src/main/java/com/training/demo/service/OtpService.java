package com.training.demo.service;

import com.training.demo.dto.request.Auth.EmailOtpRequest;
import com.training.demo.dto.request.Otp.ResendOtpRequest;
import com.training.demo.dto.request.Otp.SendOtpRequest;
import com.training.demo.entity.User;
import com.training.demo.utils.enums.OtpType;

public interface OtpService {

    /**
     * Send a new OTP to the user for the specified operation type.
     *
     * @param request request containing recipient email
     * @param type OTP type (e.g. RESET_PASSWORD, VERIFY_EMAIL)
     */
    void sendOtp(SendOtpRequest request, OtpType type);

    /**
     * Resend OTP to the user if allowed by rate-limiting rules.
     *
     * @param request request containing recipient email and OTP type
     */
    void resendOtp(ResendOtpRequest request);

    /**
     * Verify the provided OTP code and return a temporary verify key.
     *
     * @param request request containing email and OTP code
     * @return verification key string (UUID)
     */
    String verifyOtp(EmailOtpRequest request);

    /**
     * Verify user email using OTP code.
     *
     * @param request request containing email and OTP code
     */
    void verifyEmail(EmailOtpRequest request);

    /**
     * Confirm a previously issued verify key and resolve the {@link User}.
     *
     * @param verifyKey temporary verification key
     * @return user associated with the verify key if valid
     */
    User confirmVerifyKey(String verifyKey);

}
