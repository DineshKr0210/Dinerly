package com.restaurant.waitlist.backend.controller;

import com.restaurant.waitlist.backend.service.InboundSmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/twilio")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class TwilioWebhookController {

    private final InboundSmsService inboundSmsService;

    @PostMapping("/inbound-sms")
    public ResponseEntity<String> inboundSms(@RequestParam Map<String, String> params) {
        String from = params.get("From");
        String body = params.get("Body");
        String to = params.get("To");
        String messageSid = params.get("MessageSid");

        boolean processed = inboundSmsService.processInboundSms(from, body, to, messageSid);
        return ResponseEntity.ok(processed ? "OK" : "IGNORED");
    }
}
