package com.restaurant.waitlist.backend.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PointsEarningRuleRequest {
    @NotBlank
    private String action; // "dine_in", "join_waitlist", "leave_review", "refer_friend"

    @NotNull
    private Long pointsValue;

    @NotNull
    private Long restaurantId;

    private String description;

    private String icon;

    private Boolean clickable;

    private String actionUrl;
}
