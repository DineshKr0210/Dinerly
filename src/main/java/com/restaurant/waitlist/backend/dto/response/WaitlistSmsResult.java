package com.restaurant.waitlist.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WaitlistSmsResult {
    private WaitlistResponse waitlist;
    private boolean smsSent;
    private String smsError;
}

