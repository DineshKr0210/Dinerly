package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.entity.NotificationSettingsPayload;
import com.restaurant.waitlist.backend.entity.RestaurantSettings;
import com.restaurant.waitlist.backend.entity.SmsTemplate;
import com.restaurant.waitlist.backend.repository.RestaurantSettingsRepository;
import com.restaurant.waitlist.backend.repository.SmsTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SmsTemplateService {

    @Autowired
    private SmsTemplateRepository smsTemplateRepository;

    @Autowired
    private RestaurantSettingsRepository restaurantSettingsRepository;

    public SmsTemplate getTemplate(String templateType) {
        return smsTemplateRepository.findByTemplateType(templateType)
                .orElseThrow(() -> new RuntimeException("SMS template not found: " + templateType));
    }

    public List<SmsTemplate> getAllTemplates() {
        return smsTemplateRepository.findAll();
    }

    public SmsTemplate updateTemplate(Long id, String messageTemplate, String description) {
        SmsTemplate template = smsTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SMS template not found"));
        
        template.setMessageTemplate(messageTemplate);
        template.setDescription(description);
        return smsTemplateRepository.save(template);
    }

    public String formatMessage(String templateType, java.util.Map<String, String> params) {
        SmsTemplate template = getTemplate(templateType);
        String message = template.getMessageTemplate();
        
        for (java.util.Map.Entry<String, String> entry : params.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        
        return message;
    }

    public String formatMessageForRestaurant(Long restaurantId, String templateType, Map<String, String> params) {
        RestaurantSettings settings = restaurantSettingsRepository.findByRestaurantId(restaurantId)
                .orElse(null);
        NotificationSettingsPayload payload = settings != null ? settings.getNotificationSettings() : NotificationSettingsPayload.defaults();

        if ("WAITLIST_NOTIFICATION".equals(templateType)) {
            NotificationSettingsPayload.MessageTemplates templates = payload.getMessageTemplates();
            String templateText = templates.getNotifySmsTemplatePreview();
            if (templates.getNotifySmsTemplatePreview() != null && !templates.getNotifySmsTemplatePreview().isBlank()) {
                templateText = templates.getNotifySmsTemplatePreview();
            }
            params = new HashMap<>(params);
            params.putIfAbsent("restaurantName", "Brothers Café");
            return formatCustomTemplate(templateText, params);
        }
        if ("CALL_NOTIFICATION".equals(templateType)) {
            NotificationSettingsPayload.MessageTemplates templates = payload.getMessageTemplates();
            String templateText = templates.getVoiceTemplatePreview();
            if (templates.getVoiceTemplatePreview() != null && !templates.getVoiceTemplatePreview().isBlank()) {
                templateText = templates.getVoiceTemplatePreview();
            }
            params = new HashMap<>(params);
            params.putIfAbsent("restaurantName", "Brothers Café");
            return formatCustomTemplate(templateText, params);
        }
        if ("WAITLIST_APPROVED".equals(templateType)) {
            NotificationSettingsPayload.MessageTemplates templates = payload.getMessageTemplates();
            String templateText = templates.getApproveSmsTemplatePreview();
            if (templates.getApproveSmsTemplatePreview() != null && !templates.getApproveSmsTemplatePreview().isBlank()) {
                templateText = templates.getApproveSmsTemplatePreview();
            }
            params = new HashMap<>(params);
            params.putIfAbsent("restaurantName", "Brothers Café");
            return formatCustomTemplate(templateText, params);
        }
        if ("WAITLIST_JOIN_CONFIRMATION".equals(templateType)) {
            NotificationSettingsPayload.MessageTemplates templates = payload.getMessageTemplates();
            String templateText = templates.getSmsTemplatePreview();
            if (templates.getSmsTemplatePreview() != null && !templates.getSmsTemplatePreview().isBlank()) {
                templateText = templates.getSmsTemplatePreview();
            }
            params = new HashMap<>(params);
            params.putIfAbsent("restaurantName", "Brothers Café");
            return formatCustomTemplate(templateText, params);
        }
        return formatMessage(templateType, params);
    }

    public String formatCustomTemplate(String template, Map<String, String> params) {
        String message = template;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String token = "{" + entry.getKey() + "}";
            message = message.replace(token, entry.getValue() == null ? "" : entry.getValue());
        }
        return message;
    }

    public void initializeDefaultTemplates() {
        // Check and create default templates if they don't exist
        if (smsTemplateRepository.findByTemplateType("WAITLIST_NOTIFICATION").isEmpty()) {
            SmsTemplate waitlistTemplate = SmsTemplate.builder()
                    .templateType("WAITLIST_NOTIFICATION")
                    .messageTemplate("Hi {guestName}, this is {restaurantName}. Your table is ready. Please head to the host stand within the next ten minutes. We look forward to seeing you!")
                    .description("Template for notifying guests when their table is almost ready")
                    .build();
            smsTemplateRepository.save(waitlistTemplate);
        }

        if (smsTemplateRepository.findByTemplateType("SEATED_NOTIFICATION").isEmpty()) {
            SmsTemplate seatedTemplate = SmsTemplate.builder()
                    .templateType("SEATED_NOTIFICATION")
                    .messageTemplate("Hi {guestName}, your table is ready! Please proceed to the host stand. Thank you!")
                    .description("Template for confirming guest is seated")
                    .build();
            smsTemplateRepository.save(seatedTemplate);
        }
    }

    public void initializeJoinConfirmationTemplate() {
        if (smsTemplateRepository.findByTemplateType("WAITLIST_JOIN_CONFIRMATION").isEmpty()) {
            SmsTemplate joinTemplate = SmsTemplate.builder()
                    .templateType("WAITLIST_JOIN_CONFIRMATION")
                    .messageTemplate("Thank you {guestName}! You have joined the waitlist at {restaurantName}. We will notify you as soon as your table is ready. Please wait for confirmation from the restaurant.")
                    .description("Template for confirming guest has joined the waitlist")
                    .build();
            smsTemplateRepository.save(joinTemplate);
        }
    }

    public void initializeApprovedTemplate() {
        if (smsTemplateRepository.findByTemplateType("WAITLIST_APPROVED").isEmpty()) {
            SmsTemplate approvedTemplate = SmsTemplate.builder()
                    .templateType("WAITLIST_APPROVED")
                    .messageTemplate("Hi {guestName}, great news! Your reservation has been approved.{position} Estimated wait time is {estimatedWait} minutes. We look forward to serving you!")
                    .description("Template for notifying guests their reservation has been approved")
                    .build();
            smsTemplateRepository.save(approvedTemplate);
        }
    }
}

