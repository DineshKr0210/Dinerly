package com.restaurant.waitlist.backend.service.admin;

import com.restaurant.waitlist.backend.dto.response.SmsTemplateResponse;
import com.restaurant.waitlist.backend.dto.request.admin.MarketingCampaignRequest;

import java.util.List;

public interface AdminMarketingService {
    List<SmsTemplateResponse> listTemplates();
    SmsTemplateResponse updateTemplate(Long id, String messageTemplate, String description);
    int sendCampaign(MarketingCampaignRequest request);
}
