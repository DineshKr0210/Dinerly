package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.dto.request.admin.MarketingCampaignRequest;
import com.restaurant.waitlist.backend.dto.response.SmsTemplateResponse;
import com.restaurant.waitlist.backend.service.AuditLogService;
import com.restaurant.waitlist.backend.service.admin.AdminMarketingService;
import com.restaurant.waitlist.backend.service.SmsService;
import com.restaurant.waitlist.backend.service.SmsTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminMarketingServiceImpl implements AdminMarketingService {

    private final SmsTemplateService smsTemplateService;
    private final SmsService smsService;
    private final AuditLogService auditLogService;

    @Override
    public List<SmsTemplateResponse> listTemplates() {
        List<com.restaurant.waitlist.backend.entity.SmsTemplate> templates = smsTemplateService.getAllTemplates();
        List<SmsTemplateResponse> resp = new ArrayList<>();
        for (com.restaurant.waitlist.backend.entity.SmsTemplate t : templates) {
            resp.add(SmsTemplateResponse.fromSmsTemplate(t));
        }
        return resp;
    }

    @Override
    public SmsTemplateResponse updateTemplate(Long id, String messageTemplate, String description) {
        com.restaurant.waitlist.backend.entity.SmsTemplate t = smsTemplateService.updateTemplate(id, messageTemplate, description);
        auditLogService.log(0L, "UPDATE_SMS_TEMPLATE", "Updated SMS template id=" + id);
        return SmsTemplateResponse.fromSmsTemplate(t);
    }

    @Override
    public SmsTemplateResponse createTemplate(String templateType, String messageTemplate, String description) {
        com.restaurant.waitlist.backend.entity.SmsTemplate t = smsTemplateService.createTemplate(templateType, messageTemplate, description);
        auditLogService.log(0L, "CREATE_SMS_TEMPLATE", "Created SMS template type=" + templateType);
        return SmsTemplateResponse.fromSmsTemplate(t);
    }

    @Override
    public void deleteTemplate(Long id) {
        smsTemplateService.deleteTemplate(id);
        auditLogService.log(0L, "DELETE_SMS_TEMPLATE", "Deleted SMS template id=" + id);
    }

    @Override
    public int sendCampaign(MarketingCampaignRequest request) {
        if (request.getPhoneNumbers() == null || request.getPhoneNumbers().isEmpty()) return 0;
        int sent = 0;
        for (String to : request.getPhoneNumbers()) {
            try {
                String message = request.getMessage();
                if ((message == null || message.isBlank()) && request.getTemplateType() != null) {
                    if (request.getRestaurantId() != null) {
                        message = smsTemplateService.formatMessageForRestaurant(request.getRestaurantId(), request.getTemplateType(), Map.of());
                    } else {
                        message = smsTemplateService.formatMessage(request.getTemplateType(), Map.of());
                    }
                }
                if (message == null || message.isBlank()) continue;
                smsService.sendSms(to, message);
                sent++;
            } catch (Exception ignored) {
            }
        }
        auditLogService.log(request.getRestaurantId() != null ? request.getRestaurantId() : 0L,
                "SEND_MARKETING_CAMPAIGN", "Sent campaign template=" + request.getTemplateType() + " recipients=" + sent);
        return sent;
    }
}
