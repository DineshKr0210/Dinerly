package com.restaurant.waitlist.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class JoinWaitlistRequest {
    @NotNull(message = "Restaurant ID is required")
    @Positive(message = "Restaurant ID must be positive")
    private Long restaurantId;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Phone is required")
    private String phone;

    @NotNull(message = "Party size is required")
    @Positive(message = "Party size must be at least 1")
    private Integer partySize;

    private String preference;

    private String notes;
}

