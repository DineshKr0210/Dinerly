package com.restaurant.waitlist.backend.dto.request.admin;

import lombok.Data;

@Data
public class ReversePointsRequest {
    private Long userId;
    private Long amount;
    private String reason;
    private Long referenceId;
}
