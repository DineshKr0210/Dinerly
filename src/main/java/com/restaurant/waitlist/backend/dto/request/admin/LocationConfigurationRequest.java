package com.restaurant.waitlist.backend.dto.request.admin;

import lombok.Data;

@Data
public class LocationConfigurationRequest {
    private Long locationId;
    private String offerCategory;  // Default offer category per location
    private String rewardRuleKey;  // Key for default reward rule at location
    private Double discountDefault;  // Default discount value
    private Integer inventoryDefault;  // Default inventory for offers
    private String timezone;
    private String locale;
}
