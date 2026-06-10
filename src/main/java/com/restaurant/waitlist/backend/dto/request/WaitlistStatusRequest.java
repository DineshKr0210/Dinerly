package com.restaurant.waitlist.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class WaitlistStatusRequest {
    @NotNull(message = "Restaurant ID is required")
    @Positive(message = "Restaurant ID must be positive")
    private Long restaurantId;

    @NotBlank(message = "Phone is required")
    private String phone;
}

