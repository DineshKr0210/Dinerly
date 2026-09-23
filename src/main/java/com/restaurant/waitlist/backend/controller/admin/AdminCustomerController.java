package com.restaurant.waitlist.backend.controller.admin;

import com.restaurant.waitlist.backend.dto.response.admin.CustomerResponse;
import com.restaurant.waitlist.backend.dto.response.admin.CustomerSummaryResponse;
import com.restaurant.waitlist.backend.service.admin.AdminCustomerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/customers")
@RequiredArgsConstructor
@Tag(name = "Admin Customers", description = "Customer summary, list, and visit history across an admin's locations")
public class AdminCustomerController {

    private final AdminCustomerService adminCustomerService;

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomerSummaryResponse> summary(@RequestParam(required = false) Long restaurantId) {
        CustomerSummaryResponse resp = adminCustomerService.getCustomerSummary(restaurantId);
        return ResponseEntity.ok(resp);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CustomerResponse>> list(@RequestParam(required = false) Long restaurantId,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size) {
        Page<CustomerResponse> p = adminCustomerService.listCustomers(restaurantId, PageRequest.of(page, size));
        return ResponseEntity.ok(p);
    }
}
