package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.dto.response.admin.CustomerResponse;
import com.restaurant.waitlist.backend.repository.CustomerAggregation;
import com.restaurant.waitlist.backend.repository.WaitlistRepository;
import com.restaurant.waitlist.backend.service.admin.AdminCustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminCustomerServiceImpl implements AdminCustomerService {

    private final WaitlistRepository waitlistRepository;
    private final com.restaurant.waitlist.backend.repository.AuditLogRepository auditLogRepository;

    @Override
    public Page<CustomerResponse> listCustomers(Long restaurantId, Pageable pageable) {
        List<CustomerAggregation> agg = waitlistRepository.aggregateCustomers(restaurantId);
        List<CustomerResponse> items = agg.stream().map(a -> CustomerResponse.builder()
                .guest(a.getGuest())
                .contact(a.getContact())
                .visits(a.getVisits())
                .lastVisit(a.getLastVisit())
                .loyalty(a.getVisits() > 5 ? "Gold" : a.getVisits() > 2 ? "Silver" : "Bronze")
                .build()).collect(Collectors.toList());
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), items.size());
        List<CustomerResponse> pageItems = items.subList(Math.min(start, end), end);

        auditLogRepository.save(com.restaurant.waitlist.backend.entity.AuditLog.builder()
            .restaurantId(restaurantId != null ? restaurantId : 0L)
            .action("LIST_CUSTOMERS")
            .details("Listed customers count=" + items.size())
            .build());
        return new PageImpl<>(pageItems, pageable, items.size());
    }
}
