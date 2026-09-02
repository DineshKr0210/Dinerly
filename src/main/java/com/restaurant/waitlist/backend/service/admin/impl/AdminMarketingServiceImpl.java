package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.dto.request.admin.MarketingCampaignRequest;
import com.restaurant.waitlist.backend.dto.response.SmsTemplateResponse;
import com.restaurant.waitlist.backend.repository.AuditLogRepository;
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
    private final AuditLogRepository auditLogRepository;

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
        auditLogRepository.save(com.restaurant.waitlist.backend.entity.AuditLog.builder()
                .restaurantId(0L)
                .action("UPDATE_SMS_TEMPLATE")
                .details("Updated SMS template id=" + id)
                .build());
        return SmsTemplateResponse.fromSmsTemplate(t);
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
        auditLogRepository.save(com.restaurant.waitlist.backend.entity.AuditLog.builder()
                .restaurantId(request.getRestaurantId() != null ? request.getRestaurantId() : 0L)
                .action("SEND_MARKETING_CAMPAIGN")
                .details("Sent campaign template=" + request.getTemplateType() + " recipients=" + sent)
                .build());
        return sent;
    }
}
