package com.restaurant.waitlist.backend.dto.request.admin;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class RewardSettingsRequest {
    @NotNull
    private Boolean preventDuplicateRedemptionsWithinVisit;
}
