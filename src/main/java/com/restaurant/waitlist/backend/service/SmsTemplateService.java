package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.entity.SmsTemplate;
import com.restaurant.waitlist.backend.repository.SmsTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SmsTemplateService {

    @Autowired
    private SmsTemplateRepository smsTemplateRepository;

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

    public void initializeDefaultTemplates() {
        // Check and create default templates if they don't exist
        if (smsTemplateRepository.findByTemplateType("WAITLIST_NOTIFICATION").isEmpty()) {
            SmsTemplate waitlistTemplate = SmsTemplate.builder()
                    .templateType("WAITLIST_NOTIFICATION")
                    .messageTemplate("Hi {guestName}, your table is almost ready! Please come to the restaurant now.")
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
                    .messageTemplate("Thank you {guestName}! We're delighted to have you at our restaurant. You have been added to our waitlist. We'll notify you as soon as your table is ready. We appreciate your patience!")
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

