package com.restaurant.waitlist.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RedeemOfferRequest {
    @NotNull
    private Long offerId;
}
