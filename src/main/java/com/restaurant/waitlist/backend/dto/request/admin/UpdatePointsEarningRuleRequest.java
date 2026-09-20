package com.restaurant.waitlist.backend.dto.request.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePointsEarningRuleRequest {
    @NotNull
    private Long restaurantId;
    
    private String action;
    
    private Long pointsValue;
    
    private String description;
    
    private String icon;
    
    private Boolean clickable;
    
    private String actionUrl;
}

