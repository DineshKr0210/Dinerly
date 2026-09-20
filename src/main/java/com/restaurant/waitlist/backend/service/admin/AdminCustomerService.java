package com.restaurant.waitlist.backend.service.admin;

import com.restaurant.waitlist.backend.dto.response.admin.CustomerResponse;
import com.restaurant.waitlist.backend.dto.response.admin.CustomerSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminCustomerService {
    CustomerSummaryResponse getCustomerSummary(Long restaurantId);
    Page<CustomerResponse> listCustomers(Long restaurantId, Pageable pageable);
}
