package com.restaurant.waitlist.backend.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.Account;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

@Service
public class SmsService {
    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String fromPhoneNumber;

    @Autowired
    private SmsTemplateService smsTemplateService;

    private String mask(String s) {
        if (s == null) return "null";
        if (s.length() <= 4) return "****";
        return "****" + s.substring(s.length() - 4);
    }

    public void sendSms(String toPhoneNumber, String message) {
        try {
            log.debug("Initializing Twilio with accountSid={} and from={}",
                    mask(accountSid), fromPhoneNumber);
            Twilio.init(accountSid, authToken);
            Message msg = Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(fromPhoneNumber),
                    message
            ).create();
            log.info("SMS sent successfully to {}. SID={}", toPhoneNumber, msg.getSid());
        } catch (Exception e) {
            log.error("Error sending SMS to {}: {}", toPhoneNumber, e.getMessage());
            log.debug("SMS send exception", e);
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

    public String sendWaitlistNotificationSms(String phoneNumber, String guestName, String estimatedWait, Integer position) {
        Map<String, String> params = new HashMap<>();
        params.put("guestName", guestName);
        params.put("estimatedWait", estimatedWait);
        params.put("position", (position != null) ? (" Your position: " + position + ".") : "");

        String message = smsTemplateService.formatMessage("WAITLIST_NOTIFICATION", params);
        sendSms(phoneNumber, message);
        return message;
    }

    public String sendApprovedNotificationSms(String phoneNumber, String guestName, String estimatedWait, Integer position) {
        Map<String, String> params = new HashMap<>();
        params.put("guestName", guestName);
        params.put("estimatedWait", estimatedWait);
        params.put("position", (position != null) ? (" Your position: " + position + ".") : "");

        String message = smsTemplateService.formatMessage("WAITLIST_APPROVED", params);
        sendSms(phoneNumber, message);
        return message;
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

    public Map<String, String> probeAccount() {
        Map<String, String> out = new HashMap<>();
        try {
            log.debug("Probing Twilio account with accountSid={}", mask(accountSid));
            Twilio.init(accountSid, authToken);
            Account acct = Account.fetcher(accountSid).fetch();
            out.put("sid", mask(acct.getSid()));
            out.put("friendlyName", acct.getFriendlyName() != null ? acct.getFriendlyName() : "");
            out.put("status", "ok");
            return out;
        } catch (Exception e) {
            log.error("Twilio probe failed: {}", e.getMessage());
            out.put("status", "error");
            out.put("message", e.getMessage());
            return out;
        }
    }
}
