package com.restaurant.waitlist.backend.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.Account;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.HashMap;
import java.util.Map;

@Service
public class SmsService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String fromPhoneNumber;

    @Autowired
    private SmsTemplateService smsTemplateService;

    public void sendSms(String toPhoneNumber, String message) {
        try {
            Twilio.init(accountSid, authToken);
            Message msg = Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(fromPhoneNumber),
                    message
            ).create();

            System.out.println("SMS sent successfully. SID: " + msg.getSid());
        } catch (Exception e) {
            System.err.println("Error sending SMS: " + e.getMessage());
            throw new RuntimeException("Failed to send SMS notification");
        }
    }

    public void sendWaitlistNotificationSms(String phoneNumber, String guestName, String estimatedWait) {
        Map<String, String> params = new HashMap<>();
        params.put("guestName", guestName);
        params.put("estimatedWait", estimatedWait);
        params.put("position", "");

        String message = smsTemplateService.formatMessage("WAITLIST_NOTIFICATION", params);
        sendSms(phoneNumber, message);
    }

    public void sendWaitlistNotificationSms(String phoneNumber, String guestName, String estimatedWait, Integer position) {
        Map<String, String> params = new HashMap<>();
        params.put("guestName", guestName);
        params.put("estimatedWait", estimatedWait);
        params.put("position", (position != null) ? (" Your position: " + position + ".") : "");

        String message = smsTemplateService.formatMessage("WAITLIST_NOTIFICATION", params);
        sendSms(phoneNumber, message);
    }

    public void sendSeatedNotificationSms(String phoneNumber, String guestName) {
        Map<String, String> params = new HashMap<>();
        params.put("guestName", guestName);

        String message = smsTemplateService.formatMessage("SEATED_NOTIFICATION", params);
        sendSms(phoneNumber, message);
    }

    public void sendJoinConfirmationSms(String phoneNumber, String guestName) {
        Map<String, String> params = new HashMap<>();
        params.put("guestName", guestName);

        String message = smsTemplateService.formatMessage("WAITLIST_JOIN_CONFIRMATION", params);
        sendSms(phoneNumber, message);
    }
}
