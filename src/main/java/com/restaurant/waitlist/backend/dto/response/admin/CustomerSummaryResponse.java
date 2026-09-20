package com.restaurant.waitlist.backend.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerSummaryResponse {
    // Total unique guests card
    private Long totalUniqueGuests;
    private Long totalGuestsLastMonth;
    private Double totalGuestsTrendPercent;

    // New customers card
    private Long newCustomers;
    private Double newCustomersPercent;
    private Long newCustomersLastMonth;
    private Double newCustomersTrendPercent;

    // Regular customers card
    private Long regularCustomers;
    private Double regularCustomersPercent;
    private Long regularCustomersLastMonth;
    private Double regularCustomersTrendPercent;

    // Average visits card
    private Double avgVisitsPerRegular;
    private Double avgVisitsLastMonth;
    private Double avgVisitsTrendPercent;
}
