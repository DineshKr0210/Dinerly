package com.restaurant.waitlist.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSummaryResponse {
    private Long totalGuests;
    private Long waiting;
    private Long notified;
    private Long seated;
    private Long cancelled;
}

