package com.restaurant.waitlist.backend.dto.response.admin;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class OfferResponse {
    private Long id;
    private String name;
    private Long locationId;
    private String locationName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private BigDecimal value;
    private Long redemptions;
}
