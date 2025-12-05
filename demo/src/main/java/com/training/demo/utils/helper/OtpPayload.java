package com.training.demo.utils.helper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtpPayload {
    private String code;
    private int resendCount;
    private int attempts;
    private long createdAt;

    /** Increments the number of verification attempts. */
    public void incrementAttempts() {
        this.attempts++;
    }

    /** Increments the number of times OTP was resent. */
    public void incrementResend() {
        this.resendCount++;
    }
}
