package com.training.demo.validator;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.*;
import java.util.regex.Pattern;

@Documented
@Constraint(validatedBy = ValidPassword.PasswordValidator.class)
@Target({ ElementType.FIELD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {
    String message() default "Password is not in correct format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

        // Ít nhất 1 số, 1 chữ thường, 1 chữ hoa, 1 ký tự đặc biệt, không khoảng trắng, dài 8–30
        private static final String PASSWORD_REGEX =
                "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{6,30}$";

        /**
         * Implements the validation logic.
         * The state of {@code value} must not be altered.
         * <p>
         * This method can be accessed concurrently, thread-safety must be ensured
         * by the implementation.
         *
         * @param value   object to validate
         * @param context context in which the constraint is evaluated
         * @return {@code false} if {@code value} does not pass the constraint
         */
        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (value == null || value.isBlank()) {
                // để @NotBlank xử lý
                return true;
            }
            return Pattern.matches(PASSWORD_REGEX, value);
        }
    }
}
