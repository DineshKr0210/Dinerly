package com.restaurant.waitlist.backend.dto.request.admin;

import lombok.Data;

import java.util.List;

@Data
public class BulkCreditRequest {
    private List<Long> userIds;
    private Long amount;
    private String reason;
}
