package com.restaurant.waitlist.backend.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RewardItemRequest {
    @NotBlank
    private String title; // "Free coffee"

    private String description;

    @NotNull
    private Long pointsCost;

    @NotNull
    private Long restaurantId;

    private String icon;

    private String category; // "food", "beverage", "discount"

    private Boolean available;
}
