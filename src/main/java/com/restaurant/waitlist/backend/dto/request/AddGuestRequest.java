package com.restaurant.waitlist.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class AddGuestRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Phone is required")
    private String phone;

    @NotNull(message = "Party size is required")
    @Positive(message = "Party size must be at least 1")
    private Integer partySize;

    private String preference;

    private String notes;

    @NotNull(message = "Position is required")
    @Positive(message = "Position must be positive")
    private Integer position;

    @Positive(message = "Estimated wait time must be positive")
    private Integer estimatedWaitTime;
}

