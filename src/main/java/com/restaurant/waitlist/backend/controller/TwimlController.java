package com.restaurant.waitlist.backend.controller;

import com.restaurant.waitlist.backend.repository.WaitlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/twilio")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class TwimlController {

    private final WaitlistRepository waitlistRepository;

    @RequestMapping(value = "/voice-webhook", method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> voiceWebhook(@RequestParam(value = "message", required = false) String message,
                                               @RequestParam(value = "To", required = false) String to,
                                               @RequestParam(value = "From", required = false) String from,
                                               @RequestParam(value = "CallSid", required = false) String callSid,
                                               @RequestParam(value = "voice", required = false) String voice) {
        String prompt = "Hi guest, your table is ready at the restaurant. Please press 1 if you can attend, or press 2 if you cannot.";
        if (message != null && !message.isBlank()) {
            prompt = URLDecoder.decode(message, StandardCharsets.UTF_8);
        }

        String selectedVoice = resolveVoice(voice);
        String twiml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<Response>"
                + "<Say voice=\"" + selectedVoice + "\">" + prompt + "</Say>"
                + "<Gather numDigits=\"1\" action=\"/api/twilio/voice-event\" method=\"POST\">"
                + "<Say voice=\"" + selectedVoice + "\">Press 1 if you can attend, or press 2 if you cannot attend.</Say>"
                + "</Gather>"
                + "<Say voice=\"" + selectedVoice + "\">We did not receive a selection. Goodbye.</Say>"
                + "</Response>";

        return ResponseEntity.ok(twiml);
    }

    private String resolveVoice(String voice) {
        if (voice == null || voice.isBlank()) {
            return "Polly.Joanna";
        }
        String normalized = voice.trim().toLowerCase();
        if ("female".equals(normalized)) {
            return "Polly.Joanna";
        }
        if ("male".equals(normalized)) {
            return "Polly.Matthew";
        }
        return "Polly.Joanna";
    }
}
