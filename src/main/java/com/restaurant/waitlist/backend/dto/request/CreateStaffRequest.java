package com.restaurant.waitlist.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateStaffRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String role;
}
