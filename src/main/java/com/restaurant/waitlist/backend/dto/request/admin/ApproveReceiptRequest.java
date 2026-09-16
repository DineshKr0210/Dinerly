package com.restaurant.waitlist.backend.dto.request.admin;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApproveReceiptRequest {
    @NotNull(message = "Points override cannot be null")
    @Positive(message = "Points override must be positive")
    private Long pointsOverride;
    
    private String notes;
}
