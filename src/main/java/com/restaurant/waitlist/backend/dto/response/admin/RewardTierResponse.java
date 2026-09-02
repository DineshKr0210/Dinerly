package com.restaurant.waitlist.backend.dto.response.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RewardTierResponse {
    private Long id;
    private String name;
    private Integer points;
    private String perks;
}
