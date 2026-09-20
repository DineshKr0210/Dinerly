package com.restaurant.waitlist.backend.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketingSummaryResponse {
    private long activeCampaigns;
    private Double activeCampaignsTrendPercent;
    private long guestsReached;
    private Double guestsReachedPercent;
    private long redemptions;
    private long redemptionsLastMonth;
    private Double redemptionsTrendPercent;
    private long spendThisMonth;
    private Double spendTrendPercent;
    private Long locationId;
}
