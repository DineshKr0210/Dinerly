package com.restaurant.waitlist.backend.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RewardTierRequest {
    @NotBlank
    private String name; // "Silver", "Gold", "Platinum"

    @NotNull
    private Long pointsThreshold; // Min points needed

    @NotNull
    private Integer tierOrder; // 1, 2, 3

    @NotNull
    private Long restaurantId;

    private List<String> perks; // ["Free dessert", "Priority seating"]

    private String color; // silver, gold, platinum

    // Legacy fields
    private Integer points;
}
