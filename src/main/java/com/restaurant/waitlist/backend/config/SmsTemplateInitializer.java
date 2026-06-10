package com.restaurant.waitlist.backend.config;

import com.restaurant.waitlist.backend.service.SmsTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SmsTemplateInitializer {

    @Autowired
    private SmsTemplateService smsTemplateService;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeSmsTemplates() {
        try {
            smsTemplateService.initializeDefaultTemplates();
            smsTemplateService.initializeJoinConfirmationTemplate();
            System.out.println("SMS templates initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing SMS templates: " + e.getMessage());
        }
    }
}

