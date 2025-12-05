package com.training.demo.application.usecase.auth;

import com.training.demo.dto.request.Auth.RegisterRequest;
import com.training.demo.entity.Role;
import com.training.demo.entity.User;
import com.training.demo.entity.UserHasRole;
import com.training.demo.exception.BadRequestException;
import com.training.demo.repository.RoleRepository;
import com.training.demo.repository.UserRepository;
import com.training.demo.utils.constants.ApiConstants;
import com.training.demo.utils.constants.ValidationConstants;
import com.training.demo.utils.enums.RoleType;
import com.training.demo.utils.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: User Registration
 * Business logic: Create new user account with default USER role
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RegisterUseCase {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User execute(RegisterRequest request) {
        log.info("[RegisterUseCase] Registering new user: {}", request.getUsername());

        // 1. Validate uniqueness
        validateUniqueness(request);

        // 2. Validate password strength
        validatePasswordStrength(request.getPassword());

        // 3. Create user entity
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .status(UserStatus.INACTIVE)
                .verifyEmail(false)
                .build();

        // 4. Assign default USER role
        Role userRole = roleRepository.findByName(RoleType.USER)
                .orElseThrow(() -> new BadRequestException(ApiConstants.Messages.ROLE_NOT_FOUND));

        UserHasRole userHasRole = UserHasRole.builder()
                .user(user)
                .role(userRole)
                .build();

        user.getUserHasRoles().add(userHasRole);

        // 5. Save user
        User savedUser = userRepository.save(user);

        log.info("[RegisterUseCase] User registered successfully: {}", savedUser.getUsername());

        return savedUser;
    }

    private void validateUniqueness(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException(ApiConstants.Messages.USERNAME_EXISTS);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException(ApiConstants.Messages.EMAIL_EXISTS);
        }
    }

    private void validatePasswordStrength(String password) {
        if (password.length() < ValidationConstants.User.MIN_PASSWORD_LENGTH) {
            throw new BadRequestException(
                String.format("Password must be at least %d characters", 
                    ValidationConstants.User.MIN_PASSWORD_LENGTH)
            );
        }
        
        // Check for uppercase, lowercase, and digit
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
