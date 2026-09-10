package com.restaurant.waitlist.backend.dto.request.admin;

import lombok.Data;

@Data
public class CreditPointsRequest {
    private Long userId;
    private Long amount;
    private String reason;
}
