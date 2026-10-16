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
    private String mobileNumber;
    private Long offerId;
    private String discountType;
    private java.math.BigDecimal discountValue;
    private String discountLabel;
    private LocalDateTime confirmedAt;
    private LocalDateTime codeExpiresAt;
    
    // QR Code: Base64 encoded PNG image or data URI
    // For validation/scanning purposes
    private String qrCodeDataUri; // data:image/png;base64,...
}
