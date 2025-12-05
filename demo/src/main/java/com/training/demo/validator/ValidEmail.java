package com.training.demo.validator;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.annotation.*;
import java.util.Arrays;

@Documented
@Constraint(validatedBy = ValidEmail.CustomEmailValidator.class)
@Target({ ElementType.FIELD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEmail {
    String message() default "Email is not in correct format.";
    Class<?>[] groups() default {};
    Class<? extends jakarta.validation.Payload>[] payload() default {};
    String[] allDomain() default {"gmail.com", "yahoo.com"};

    class CustomEmailValidator implements ConstraintValidator<ValidEmail, String> {

        private String[] allDomain;

        @Override
        public void initialize(ValidEmail constraintAnnotation) {
            this.allDomain = constraintAnnotation.allDomain();
        }

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
                // Bỏ qua check format, để @NotBlank xử lý
                return true;
            }

            // check format chung: phải có @
            int atIndex = value.indexOf("@");
            if (atIndex < 1 || atIndex == value.length() - 1) {
                return false;
            }

            String domain = value.substring(atIndex + 1);
            return Arrays.asList(allDomain).contains(domain);
        }
    }
}
