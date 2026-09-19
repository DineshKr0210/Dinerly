package com.restaurant.waitlist.backend.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffTokenVerificationResponse {
    private Boolean valid;
    private String message;
    private Long staffId;
    private String staffName;
    private String staffEmail;
    private String restaurantName;
}

