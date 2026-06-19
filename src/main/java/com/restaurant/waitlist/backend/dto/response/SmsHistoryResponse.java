package com.restaurant.waitlist.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmsHistoryResponse {
    private String smsMessage;
    private String smsStatus;
    private String smsError;
    private LocalDateTime smsSentAt;
}

