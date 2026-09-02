package com.restaurant.waitlist.backend.service.admin;

import com.restaurant.waitlist.backend.dto.request.admin.CampaignRequest;
import com.restaurant.waitlist.backend.dto.response.admin.CampaignResponse;
import com.restaurant.waitlist.backend.dto.response.admin.MarketingSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminCampaignService {
    CampaignResponse createCampaign(CampaignRequest req);
    CampaignResponse updateCampaign(Long id, CampaignRequest req);
    Page<CampaignResponse> listCampaigns(Pageable pageable);
    Page<CampaignResponse> listCampaigns(Pageable pageable, Long locationId, String status, String channel);
    MarketingSummaryResponse getMarketingSummary(Long locationId);
    CampaignResponse getCampaign(Long id);
    CampaignResponse publishCampaign(Long id, boolean immediate) throws Exception;
}
