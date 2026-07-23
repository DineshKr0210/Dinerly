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
public class InboundSmsService {

    private final WaitlistRepository waitlistRepository;

    @Transactional
    public boolean processInboundSms(String fromPhoneNumber, String body, String toPhoneNumber, String messageSid) {
        if (fromPhoneNumber == null || fromPhoneNumber.isBlank() || body == null || body.isBlank()) {
            log.warn("Ignoring inbound SMS with missing sender or body");
            return false;
        }

        String normalizedFrom = normalizePhone(fromPhoneNumber);
        Optional<Waitlist> latestWaitlist = waitlistRepository.findFirstByGuestPhoneOrderByIdDesc(normalizedFrom);
        if (latestWaitlist.isEmpty()) {
            log.warn("No waitlist entry found for inbound SMS from {}", normalizedFrom);
            return false;
        }

        Waitlist waitlist = latestWaitlist.get();
        waitlist.setLatestCustomerReply(body.trim());
        waitlist.setCustomerReplyReceivedAt(LocalDateTime.now());
        waitlist.setCustomerReplySid(messageSid);

        log.info("Captured customer reply from {} for waitlist {}: {}", normalizedFrom, waitlist.getId(), body.trim());

        waitlistRepository.save(waitlist);
        return true;
    }

    private String normalizePhone(String value) {
        if (value == null) return null;

        String s = value.trim();
        boolean hadPlus = s.startsWith("+");

        // remove everything except digits
        String digits = s.replaceAll("[^0-9]", "");

        if (digits.isEmpty()) return s;

        // if original had plus, return +<digits>
        if (hadPlus) {
            return "+" + digits;
        }

        // common Indian formats:
        // 10 digits -> assume +91
        if (digits.length() == 10) {
            return "+91" + digits;
        }

        // leading 0 + 10 digits (11 total) -> drop leading 0 and add +91
        if (digits.length() == 11 && digits.startsWith("0")) {
            return "+91" + digits.substring(1);
        }

        // leading country code without plus e.g. 919876543210 -> add +
        if (digits.length() >= 11 && (digits.startsWith("91") || digits.length() > 10)) {
            return "+" + digits;
        }

        // fallback: return raw digits
        return digits;
    }
}
