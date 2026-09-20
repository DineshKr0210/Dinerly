package com.restaurant.waitlist.backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidStaffRoleValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidStaffRole {
    String message() default "Invalid staff role. Valid roles are: ADMIN, MANAGER, HOST";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
