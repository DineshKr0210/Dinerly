package com.restaurant.waitlist.backend.dto.response.admin;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedemptionResponse {
    private Long id;
    private String itemRedeemed;
    private String location;
    private String guest;
    private LocalDateTime redeemedAt;
    private BigDecimal value;
}
