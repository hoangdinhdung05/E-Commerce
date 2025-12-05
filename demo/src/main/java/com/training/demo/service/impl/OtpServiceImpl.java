package com.training.demo.service.impl;

import com.training.demo.config.OtpProperties;
import com.training.demo.dto.response.Email.EmailDTO;
import com.training.demo.dto.request.Auth.EmailOtpRequest;
import com.training.demo.dto.request.Otp.ResendOtpRequest;
import com.training.demo.dto.request.Otp.SendOtpRequest;
import com.training.demo.entity.User;
import com.training.demo.exception.BadRequestException;
import com.training.demo.exception.NotFoundException;
import com.training.demo.repository.UserRepository;
import com.training.demo.service.MailService;
import com.training.demo.service.OtpService;
import com.training.demo.service.RedisService;
import com.training.demo.utils.OtpRedisKeyUtil;
import com.training.demo.utils.enums.OtpType;
import com.training.demo.utils.helper.OtpEmailTemplate;
import com.training.demo.utils.helper.OtpPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import static com.training.demo.utils.OtpRedisKeyUtil.otpCountKey;

@Service
@Slf4j
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final RedisService redisService;
    private final UserRepository userRepository;
    private final MailService mailService;
    private final OtpProperties otpProperties;

    /**
     * Send a new OTP to the user for the specified operation type.
     *
     * @param request request containing recipient email
     * @param type    OTP type (e.g. RESET_PASSWORD, VERIFY_EMAIL)
     */
    @Override
    @Transactional
    public void sendOtp(SendOtpRequest request, OtpType type) {
        log.info("Sending otp to email:{} and type: {}", request.getEmail(), type);

        //check user and count
        User user = getUserByEmail(request.getEmail());
        checkSendAllowed(request.getEmail(), type);

        //create otp
        String otp = generatedVerifyKey(otpProperties.getOtpLength());
        OtpPayload payload = new OtpPayload(otp, 0, 0, System.currentTimeMillis());

        //insert to redis
        storeOtp(request.getEmail(), payload, type);
        incrementSendCount(request.getEmail(), type);

        //send otp to user
        sendOtpEmail(user, payload, type);
    }

    /**
     * Resend OTP to the user if allowed by rate-limiting rules.
     *
     * @param request request containing recipient email and OTP type
     */
    @Override
    @Transactional
    public void resendOtp(ResendOtpRequest request) {
        log.info("Resend OTP running");
        sendOtp(new SendOtpRequest(request.getEmail()), request.getType());
    }

    /**
     * Verify the provided OTP code and return a temporary verify key.
     *
     * @param request request containing email and OTP code
     * @return verification key string (UUID)
     */
    @Transactional
    @Override
    public String verifyOtp(EmailOtpRequest request) {
        log.info("Verify OTP running");

        validateOtp(request.getEmail(), request.getOtp(), OtpType.RESET_PASSWORD);
        String verifyKey = createVerifyKey(request.getEmail());
        log.info("Verified OTP for email={}, verifyKey={}", request.getEmail(), verifyKey);
        return verifyKey;
    }

    /**
     * Verify user email using OTP code.
     *
     * @param request request containing email and OTP code
     */
    @Transactional
    @Override
    public void verifyEmail(EmailOtpRequest request) {
        log.info("Verify email running with email: {}", request.getEmail());

        validateOtp(request.getEmail(), request.getOtp(), OtpType.VERIFY_EMAIL);

        User user = getUserByEmail(request.getEmail());
        user.setVerifyEmail(true);
        userRepository.save(user);

        log.debug("Before save: emailVerified={}", user.isVerifyEmail());

        log.info("User {} verified successfully", user.getEmail());
    }

    /**
     * Confirm a previously issued verify key and resolve the {@link User}.
     *
     * @param verifyKey temporary verification key
     * @return user associated with the verify key if valid
     */
    @Transactional
    @Override
    public User confirmVerifyKey(String verifyKey) {
        log.info("Confirm verify key running");

        String email = (String) redisService.get(OtpRedisKeyUtil.verifyKey(verifyKey));
        if (email == null) {
            throw new NotFoundException("Verify key invalid or expired.");
        }

        User user = getUserByEmail(email);
        redisService.delete(OtpRedisKeyUtil.verifyKey(verifyKey));

        log.info("Confirmed verifyKey={} for user={}", verifyKey, user.getId());
        return user;
    }

    //========== PRIVATE METHOD =========//
    private String generatedVerifyKey(int length) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for(int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private int parseCount(Optional<String> countOpt) {
        return countOpt.map(Integer::parseInt).orElse(0);
    }

    private void checkSendAllowed(String email, OtpType type) {
        String otpKey = OtpRedisKeyUtil.otpKey(email, type);

        // Nếu OTP key còn tồn tại thì nghĩa là OTP chưa hết hạn
        if (redisService.exists(otpKey)) {
            throw new BadRequestException("OTP already sent. Please wait before requesting again.");
        }

        String countKey = otpCountKey(email, type);
        int count = parseCount(redisService.get(countKey, String.class));

        if (count >= otpProperties.getMaxSendCount()) {
            throw new BadRequestException("You have sent OTP too many times. Try again later.");
        }
    }

    private void storeOtp(String email, OtpPayload payload, OtpType type) {
        String otpKey = OtpRedisKeyUtil.otpKey(email, type);
        log.info("OtpPayload: {}", payload);

        redisService.set(otpKey, payload, otpProperties.getExpiryMinutes(), TimeUnit.MINUTES);
    }

    private void incrementSendCount(String email, OtpType type) {
        String countKey = otpCountKey(email, type);
        int count = parseCount(redisService.get(countKey, String.class));
        redisService.set(countKey, String.valueOf(count + 1),
                otpProperties.getResendLimitMinutes(), TimeUnit.MINUTES);
    }

    private void sendOtpEmail(User user, OtpPayload payload, OtpType type) {
        EmailDTO email = EmailDTO.builder()
                .to(List.of(user.getEmail()))
                .subject("Mã OTP xác thực")
                .textContent(OtpEmailTemplate.buildContent(user, payload.getCode(), type, otpProperties.getExpiryMinutes()))
                .isHtml(false)
                .build();
        mailService.sendEmailAsync(email);
    }

    private void validateOtp(String email, String inputOtp, OtpType type) {
        String otpKey = OtpRedisKeyUtil.otpKey(email, type);
        Optional<OtpPayload> payload = redisService.get(otpKey, OtpPayload.class);

        if (payload.isEmpty()) {
            log.error("OTP not found in Redis for key={}", otpKey);
            throw new NotFoundException("OTP expired or not found.");
        }

        log.debug("Validating OTP: input={} stored={}", inputOtp, payload.get().getCode());

        if (!payload.get().getCode().equals(inputOtp)) {
            throw new BadRequestException("Invalid OTP");
        }
    }

    private String createVerifyKey(String email) {
        String verifyKey = UUID.randomUUID().toString();
        String redisKey = OtpRedisKeyUtil.verifyKey(verifyKey);
        redisService.set(redisKey, email, otpProperties.getVerifyKeyExpiryMinutes(), TimeUnit.MINUTES);
        return verifyKey;
    }
}
