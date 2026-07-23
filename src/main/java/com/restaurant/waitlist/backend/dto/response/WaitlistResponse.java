package com.restaurant.waitlist.backend.dto.response;

import com.restaurant.waitlist.backend.entity.Waitlist;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaitlistResponse {
    private Long id;
    private String guestName;
    private String guestPhone;
    private Integer partySize;
    private String preference;
    private String notes;
    private Waitlist.WaitlistStatus status;
    private Integer position;
    private Integer estimatedWaitTime;
    private LocalDateTime joinedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime notifiedAt;
    private LocalDateTime seatedAt;
    private LocalDateTime cancelledAt;
    private String smsStatus;
    private String smsMessage;
    private String smsError;
    private String latestCustomerReply;
    private String customerReplyDescription;
    private LocalDateTime customerReplyReceivedAt;
    private String customerReplySid;
    private String latestVoiceReply;
    private String callStatus;
    private String callResponse;
    private LocalDateTime voiceReplyReceivedAt;
    private String voiceReplyDigits;
    private String tableName;

    public static WaitlistResponse fromWaitlist(Waitlist waitlist) {
        String customerReplyDescription = mapReplyValue(waitlist.getLatestCustomerReply());
        String callResponse = mapVoiceReplyValue(waitlist.getLatestVoiceReply(), waitlist.getVoiceReplyDigits());

        return WaitlistResponse.builder()
                .id(waitlist.getId())
                .guestName(waitlist.getGuestName())
                .guestPhone(waitlist.getGuestPhone())
                .partySize(waitlist.getPartySize())
                .preference(waitlist.getPreference())
                .notes(waitlist.getNotes())
                .status(waitlist.getStatus())
                .position(waitlist.getPosition())
                .estimatedWaitTime(waitlist.getEstimatedWaitTime())
                .joinedAt(waitlist.getJoinedAt())
                .approvedAt(waitlist.getApprovedAt())
                .notifiedAt(waitlist.getNotifiedAt())
                .seatedAt(waitlist.getSeatedAt())
                .cancelledAt(waitlist.getCancelledAt())
                .smsStatus(waitlist.getSmsStatus())
                .smsMessage(waitlist.getSmsMessage())
                .smsError(waitlist.getSmsError())
                .latestCustomerReply(waitlist.getLatestCustomerReply())
                .customerReplyDescription(customerReplyDescription)
                .customerReplyReceivedAt(waitlist.getCustomerReplyReceivedAt())
                .customerReplySid(waitlist.getCustomerReplySid())
                .latestVoiceReply(waitlist.getLatestVoiceReply())
                .callStatus(waitlist.getVoiceReplyReceivedAt() != null ? "CALL_RESPONSE_RECEIVED" : null)
                .callResponse(callResponse)
                .voiceReplyReceivedAt(waitlist.getVoiceReplyReceivedAt())
                .voiceReplyDigits(waitlist.getVoiceReplyDigits())
                .tableName(waitlist.getTableName())
                .build();
    }

    private static String mapReplyValue(String reply) {
        if (reply == null || reply.isBlank()) {
            return null;
        }

        String normalized = reply.trim();
        switch (normalized) {
            case "1":
                return "On my way";
            case "2":
                return "Arriving in 5 minutes";
            case "3":
                return "Unable to make it";
            default:
                return normalized;
        }
    }

    private static String mapVoiceReplyValue(String latestVoiceReply, String digits) {
        if (latestVoiceReply != null && !latestVoiceReply.isBlank()) {
            return latestVoiceReply;
        }
        if (digits == null || digits.isBlank()) {
            return null;
        }
        return mapReplyValue(digits);
    }
}

