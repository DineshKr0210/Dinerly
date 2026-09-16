package com.restaurant.waitlist.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RedeemRewardResponse {
    private Long redemptionId;
    private String redemptionCode; // 6-digit code for receipt
    private LocalDateTime expiresAt;
    private String message;
    private Long newPointsBalance;
    private String rewardTitle;
}
