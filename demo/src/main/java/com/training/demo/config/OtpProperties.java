package com.training.demo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "otp")
public class OtpProperties {
    private int expiryMinutes;
    private int verifyKeyExpiryMinutes;
    private int maxSendCount;
    private int resendLimitMinutes;
    private int otpLength;
}
