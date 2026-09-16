package com.restaurant.waitlist.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RedeemRewardRequest {
    @NotNull
    private Long rewardItemId;

    @NotNull
    private Long restaurantId;
}
