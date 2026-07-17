package com.restaurant.waitlist.backend.controller;

import com.restaurant.waitlist.backend.service.InboundVoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/twilio")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class TwilioVoiceWebhookController {

    private final InboundVoiceService inboundVoiceService;

    @PostMapping("/voice-event")
    public ResponseEntity<String> handleVoiceEvent(@RequestParam Map<String, String> params) {
        String from = params.get("From");
        String to = params.get("To");
        String callSid = params.get("CallSid");
        String digits = params.get("Digits");
        String recordingUrl = params.get("RecordingUrl");
        String callStatus = params.get("CallStatus");

        boolean processed = inboundVoiceService.processInboundVoice(from, to, callSid, digits, recordingUrl, callStatus);
        return ResponseEntity.ok(processed ? "OK" : "IGNORED");
    }
}
