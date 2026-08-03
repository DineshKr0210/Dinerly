package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.dto.response.SendCallResponse;
import com.restaurant.waitlist.backend.entity.NotificationSettingsPayload;
import com.restaurant.waitlist.backend.entity.RestaurantSettings;
import com.restaurant.waitlist.backend.repository.RestaurantSettingsRepository;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


import java.time.LocalDate;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class SmsService {
    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String fromPhoneNumber;

    @Value("${twilio.voice-webhook-url:}")
    private String voiceWebhookUrl;

    @Value("${twilio.voice-webhook-action-url:}")
    private String voiceWebhookActionUrl;

    @Autowired
    private SmsTemplateService smsTemplateService;

    @Autowired
    private RestaurantSettingsRepository restaurantSettingsRepository;

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

    public SendCallResponse makePhoneCall(Long restaurantId, String toPhoneNumber, String message, String guestName) {
        if (toPhoneNumber == null || toPhoneNumber.isBlank()) {
            throw new RuntimeException("Guest phone number is required");
        }
        if (voiceWebhookUrl == null || voiceWebhookUrl.isBlank()) {
            throw new RuntimeException("Twilio voice webhook URL is not configured");
        }

        try {
            String voiceUrl = voiceWebhookUrl;
            String callMessage = message;
            String selectedVoice = "male";
            if (callMessage == null || callMessage.isBlank()) {
                callMessage = "Hi, this is Brothers Café calling to let you know your table is ready. Please head to the host stand within the next ten minutes. We look forward to seeing you!";
            }
            if (restaurantId != null) {
                Map<String, String> params = new HashMap<>();
                params.put("guestName", guestName != null && !guestName.isBlank() ? guestName : "guest");
                params.put("restaurantName", "Brothers Café");
                callMessage = smsTemplateService.formatMessageForRestaurant(restaurantId, "CALL_NOTIFICATION", params);
                RestaurantSettings settings = restaurantSettingsRepository.findByRestaurantId(restaurantId).orElse(null);
                NotificationSettingsPayload payload = settings != null ? settings.getNotificationSettings() : NotificationSettingsPayload.defaults();
                if (payload.getMessageTemplates() != null && payload.getMessageTemplates().getVoice() != null && !payload.getMessageTemplates().getVoice().isBlank()) {
                    selectedVoice = payload.getMessageTemplates().getVoice().toLowerCase();
                }
            }
            if (callMessage != null && !callMessage.isBlank()) {
                String encoded = URLEncoder.encode(callMessage, StandardCharsets.UTF_8);
                String normalizedVoice = selectedVoice != null ? selectedVoice.toLowerCase() : "male";
                if ("female".equals(normalizedVoice)) {
                    normalizedVoice = "female";
                } else {
                    normalizedVoice = "male";
                }
                voiceUrl = voiceWebhookUrl + (voiceWebhookUrl.contains("?") ? "&" : "?") + "message=" + encoded + "&voice=" + normalizedVoice;
            }
            log.debug("Placing Twilio voice call to {} with webhook {} and action {}", toPhoneNumber, voiceUrl, voiceWebhookActionUrl);
            Twilio.init(accountSid, authToken);
            com.twilio.rest.api.v2010.account.CallCreator creator = Call.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(fromPhoneNumber),
                    URI.create(voiceUrl)
            );
            if (voiceWebhookActionUrl != null && !voiceWebhookActionUrl.isBlank()) {
                creator.setStatusCallback(URI.create(voiceWebhookActionUrl));
                creator.setStatusCallbackMethod(com.twilio.http.HttpMethod.POST);
            }
            Call call = creator.create();

            log.info("Phone call initiated to {}. SID={}", toPhoneNumber, call.getSid());
            return SendCallResponse.builder()
                    .callInitiated(true)
                    .status(call.getStatus() != null ? call.getStatus().toString() : "queued")
                    .sid(call.getSid())
                    .build();
        } catch (Exception e) {
            log.error("Error initiating voice call to {}: {}", toPhoneNumber, e.getMessage());
            log.debug("Voice call exception", e);
            throw new RuntimeException("Failed to initiate phone call");
        }
    }

    public double getCurrentMonthEstimatedCharge() {
        return getCurrentMonthEstimatedCharge(null);
    }

    public void sendWaitlistNotificationSms(String phoneNumber, String guestName, String estimatedWait) {
        Map<String, String> params = new HashMap<>();
        params.put("guestName", guestName);
        params.put("estimatedWait", estimatedWait);
        params.put("position", "");

        String message = smsTemplateService.formatMessage("WAITLIST_NOTIFICATION", params);
        sendSms(phoneNumber, message);
    }

    public String sendWaitlistNotificationSms(Long restaurantId, String phoneNumber, String guestName, String estimatedWait, Integer position) {
        Map<String, String> params = new HashMap<>();
        params.put("guestName", guestName);
        params.put("estimatedWait", estimatedWait != null ? estimatedWait : "");
        params.put("position", position != null ? " Your position is " + position + "." : "");
        params.put("restaurantName", "Brothers Café");

        String message = smsTemplateService.formatMessageForRestaurant(restaurantId, "WAITLIST_NOTIFICATION", params);
        sendSms(phoneNumber, message);
        return message;
    }

    public String sendApprovedNotificationSms(Long restaurantId, String phoneNumber, String guestName, String estimatedWait, Integer position) {
        Map<String, String> params = new HashMap<>();
        params.put("guestName", guestName);
        params.put("estimatedWait", estimatedWait != null ? estimatedWait : "");
        params.put("position", (position != null) ? (" Your position is " + position + ".") : "");
        params.put("restaurantName", "Brothers Café");

        String message = smsTemplateService.formatMessageForRestaurant(restaurantId, "WAITLIST_APPROVED", params);
        sendSms(phoneNumber, message);
        return message;
    }

    public void sendSeatedNotificationSms(String phoneNumber, String guestName) {
        Map<String, String> params = new HashMap<>();
        params.put("guestName", guestName);

        String message = smsTemplateService.formatMessage("SEATED_NOTIFICATION", params);
        sendSms(phoneNumber, message);
    }

    public String sendJoinConfirmationSms(Long restaurantId, String phoneNumber, String guestName) {
        Map<String, String> params = new HashMap<>();
        params.put("guestName", guestName);
        params.put("restaurantName", "Brothers Café");

        String message = smsTemplateService.formatMessageForRestaurant(restaurantId, "WAITLIST_JOIN_CONFIRMATION", params);
        sendSms(phoneNumber, message);
        return message;
    }


    public double getCurrentMonthEstimatedCharge(String usageFilter) {
    try {

        LocalDate now = LocalDate.now();
        LocalDate start = now.withDayOfMonth(1);
        LocalDate end = now.withDayOfMonth(now.lengthOfMonth());

        List<String> categories = new ArrayList<>();

        if (usageFilter == null || usageFilter.isBlank()) {

            // Total = SMS + Voice only (same as your UI)
            categories.add("sms-outbound-longcode");
            categories.add("sms-inbound-longcode");
            categories.add("calls");

        } else if ("sms".equalsIgnoreCase(usageFilter)) {

            categories.add("sms-outbound-longcode");
            categories.add("sms-inbound-longcode");

        } else if ("call".equalsIgnoreCase(usageFilter)
                || "voice".equalsIgnoreCase(usageFilter)) {

            categories.add("calls");

        } else {

            categories.add(usageFilter.toLowerCase());

        }

        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        double total = 0.0;

        for (String category : categories) {

            String nextPageUrl = String.format(
                    "https://api.twilio.com/2010-04-01/Accounts/%s/Usage/Records.json"
                            + "?Category=%s"
                            + "&StartDate=%s"
                            + "&EndDate=%s"
                            + "&PageSize=1000",
                    accountSid,
                    URLEncoder.encode(category, StandardCharsets.UTF_8),
                    start,
                    end);

            while (nextPageUrl != null && !nextPageUrl.isBlank()) {

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(nextPageUrl))
                        .header("Accept", "application/json")
                        .header("Authorization", "Basic "
                                + Base64.getEncoder()
                                .encodeToString((accountSid + ":" + authToken).getBytes()))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() < 200 || response.statusCode() >= 300) {

                    log.warn("Twilio Usage API failed. Category={} Status={} Body={}",
                            category,
                            response.statusCode(),
                            response.body());

                    break;
                }

                JsonNode root = mapper.readTree(response.body());

                JsonNode usageRecords = root.get("usage_records");

                if (usageRecords != null && usageRecords.isArray()) {

                    for (JsonNode record : usageRecords) {

                        double price = 0.0;

                        if (record.hasNonNull("price")) {

                            try {

                                price = Double.parseDouble(
                                        record.get("price")
                                                .asText()
                                                .replaceAll("[^0-9.-]", ""));

                            } catch (Exception ignored) {
                            }

                        }

                        total += Math.abs(price);
                    }

                }

                nextPageUrl = root.path("next_page_uri").asText("");

                if (!nextPageUrl.isBlank()) {
                    nextPageUrl = "https://api.twilio.com" + nextPageUrl;
                } else {
                    nextPageUrl = null;
                }
            }
        }

        return Math.round(total * 100.0) / 100.0;

    } catch (Exception ex) {

        log.error("Unable to fetch Twilio usage charges", ex);
        return 0.0;
    }
}
}
