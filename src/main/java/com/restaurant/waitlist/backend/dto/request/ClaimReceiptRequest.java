package com.restaurant.waitlist.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClaimReceiptRequest {
    @NotNull
    private Long restaurantId;

    private String receiptAmount; // From OCR or manual

    private String receiptDate; // From OCR or manual
}
