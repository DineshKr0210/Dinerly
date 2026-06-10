package com.restaurant.waitlist.backend.dto.request;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class NotifyGuestRequest {
    @Positive(message = "Estimated wait time must be positive")
    private Integer estimatedWaitTime;
}

