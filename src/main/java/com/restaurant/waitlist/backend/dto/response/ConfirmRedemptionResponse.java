package com.restaurant.waitlist.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ConfirmRedemptionResponse {
    private Long redemptionId;
    private String redemptionCode;
    private String status;
    private String offerName;
    private String offerDescription;
    private Long userId;
    private String userEmail;
    private Long offerId;
    private String discountType;
    private java.math.BigDecimal discountValue;
    private String discountLabel;
    private LocalDateTime confirmedAt;
    private LocalDateTime codeExpiresAt;
}
