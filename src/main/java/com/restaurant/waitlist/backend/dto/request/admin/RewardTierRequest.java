package com.restaurant.waitlist.backend.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RewardTierRequest {
    @NotBlank
    private String name;

    @NotNull
    private Integer points;

    private String perks;
}
