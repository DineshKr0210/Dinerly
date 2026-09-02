package com.restaurant.waitlist.backend.dto.request.admin;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AdminStaffRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String role;

    @NotNull
    @Positive
    private Long locationId;

    @NotBlank
    @Email
    private String email;
}
