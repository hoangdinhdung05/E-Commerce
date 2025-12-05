package com.training.demo.application.usecase.user;

import com.training.demo.dto.request.User.ChangePasswordRequest;
import com.training.demo.entity.User;
import com.training.demo.exception.BadRequestException;
import com.training.demo.exception.NotFoundException;
import com.training.demo.repository.UserRepository;
import com.training.demo.utils.constants.ApiConstants;
import com.training.demo.utils.constants.ValidationConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Change User Password
 * Business logic: Validate old password, validate new password strength, update password
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChangePasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void execute(Long userId, ChangePasswordRequest request) {
        log.info("[ChangePasswordUseCase] Changing password for user id: {}", userId);

        // 1. Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                    String.format(ApiConstants.Messages.USER_NOT_FOUND_ID, userId)
                ));

        // 2. Validate old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadRequestException(ApiConstants.Messages.INVALID_OLD_PASSWORD);
        }

        // 3. Validate new password is different
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new BadRequestException("New password must be different from old password");
        }

        // 4. Validate new password strength
        validatePasswordStrength(request.getNewPassword());

        // 5. Validate password confirmation
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Password confirmation does not match");
        }

        // 6. Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("[ChangePasswordUseCase] Password changed successfully for user: {}", user.getUsername());
    }

    private void validatePasswordStrength(String password) {
        if (password.length() < ValidationConstants.User.MIN_PASSWORD_LENGTH) {
            throw new BadRequestException(
                String.format("Password must be at least %d characters", 
                    ValidationConstants.User.MIN_PASSWORD_LENGTH)
            );
        }
        
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);

        if (!hasUpper || !hasLower || !hasDigit) {
            throw new BadRequestException(
                "Password must contain at least one uppercase letter, one lowercase letter, and one digit"
            );
        }
    }
}
