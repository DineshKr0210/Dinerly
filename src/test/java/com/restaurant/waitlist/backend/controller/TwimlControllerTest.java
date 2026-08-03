package com.restaurant.waitlist.backend.controller;

import com.restaurant.waitlist.backend.repository.WaitlistRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class TwimlControllerTest {

    @Test
    void voiceWebhookUsesConfiguredVoiceFromRequest() {
        TwimlController controller = new TwimlController(mock(WaitlistRepository.class));

        ResponseEntity<String> response = controller.voiceWebhook("Hello guest", null, null, null, "female");

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertTrue(response.getBody() != null && response.getBody().contains("Polly.Joanna"));
    }
}
