package com.restaurant.waitlist.backend.validation;

import com.restaurant.waitlist.backend.entity.StaffRole;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidStaffRoleValidator implements ConstraintValidator<ValidStaffRole, String> {

    @Override
    public void initialize(ValidStaffRole annotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Null values are handled by @NotBlank/@NotNull annotations
        if (value == null || value.isBlank()) {
            return true;
        }
        return StaffRole.isValid(value);
    }
}
