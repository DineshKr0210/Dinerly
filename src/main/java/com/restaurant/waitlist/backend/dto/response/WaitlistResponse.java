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
    private String tableName;

    public static WaitlistResponse fromWaitlist(Waitlist waitlist) {
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
                .tableName(waitlist.getTableName())
                .build();
    }
}

