package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.entity.Waitlist;
import com.restaurant.waitlist.backend.repository.WaitlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InboundVoiceService {

    private final WaitlistRepository waitlistRepository;

    @Transactional
    public boolean processInboundVoice(String fromPhoneNumber, String toPhoneNumber, String callSid, String digits, String recordingUrl, String callStatus) {
        if (fromPhoneNumber == null || fromPhoneNumber.isBlank()) {
            log.warn("Ignoring inbound voice event with missing from");
            return false;
        }

        Optional<Waitlist> latestWaitlist = waitlistRepository.findFirstByGuestPhoneOrderByIdDesc(normalizePhone(fromPhoneNumber));
        if (latestWaitlist.isEmpty()) {
            log.warn("No waitlist entry found for inbound voice from {}", fromPhoneNumber);
            return false;
        }

        Waitlist waitlist = latestWaitlist.get();
        waitlist.setVoiceReplyReceivedAt(LocalDateTime.now());
        waitlist.setVoiceReplyDigits(digits);
        waitlist.setLatestVoiceReply(mapVoiceReply(digits, callStatus));
        waitlistRepository.save(waitlist);

        log.info("Captured voice response for waitlist {} from {}: digits={}, status={}", waitlist.getId(), fromPhoneNumber, digits, callStatus);
        return true;
    }

    private String mapVoiceReply(String digits, String callStatus) {
        if (digits != null) {
            switch (digits.trim()) {
                case "1":
                    return "Confirmed attending";
                case "2":
                    return "Cannot attend";
                default:
                    return "Pressed " + digits;
            }
        }
        return callStatus != null ? callStatus : "VOICE_EVENT";
    }

    private String normalizePhone(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.replace(" ", "").trim();
        if (normalized.startsWith("+")) {
            return normalized;
        }
        if (normalized.startsWith("0")) {
            return "+91" + normalized.substring(1);
        }
        return normalized;
    }
}
