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
    private long guestsReached;
    private long redemptions;
    private long spendThisMonth;
    private Long locationId;
}
