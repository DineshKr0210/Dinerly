package com.restaurant.waitlist.backend.dto.request.admin;

import com.restaurant.waitlist.backend.validation.ValidStaffRole;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AdminStaffRequest {
    @NotBlank
    private String name;

    @NotBlank
    @ValidStaffRole
    private String role;

    @NotNull
    @Positive
    private Long locationId;

    @NotBlank
    @Email
    private String email;
}
