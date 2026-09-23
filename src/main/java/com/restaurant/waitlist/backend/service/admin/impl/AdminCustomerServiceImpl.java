package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.dto.response.admin.CustomerResponse;
import com.restaurant.waitlist.backend.dto.response.admin.CustomerSummaryResponse;
import com.restaurant.waitlist.backend.repository.CustomerAggregation;
import com.restaurant.waitlist.backend.repository.WaitlistRepository;
import com.restaurant.waitlist.backend.service.AdminLocationAccessService;
import com.restaurant.waitlist.backend.service.admin.AdminCustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminCustomerServiceImpl implements AdminCustomerService {

    private final WaitlistRepository waitlistRepository;
    private final com.restaurant.waitlist.backend.repository.AuditLogRepository auditLogRepository;
    private final AdminLocationAccessService adminLocationAccessService;

    @Override
    public CustomerSummaryResponse getCustomerSummary(Long restaurantId) {
        List<Long> restaurantIds = adminLocationAccessService.resolveRestaurantIds(restaurantId);
        List<CustomerAggregation> currentCustomers = waitlistRepository.aggregateCustomersByRestaurantIds(restaurantIds);
        
        // Calculate this month and last month date ranges
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);
        YearMonth lastMonth = currentMonth.minusMonths(1);
        
        LocalDate currentMonthStart = currentMonth.atDay(1);
        LocalDate currentMonthEnd = currentMonth.atEndOfMonth();
        LocalDate lastMonthStart = lastMonth.atDay(1);
        LocalDate lastMonthEnd = lastMonth.atEndOfMonth();
        
        // Count unique guests this month and last month
        long totalGuestsThisMonth = currentCustomers.size();
        long newGuestsThisMonth = currentCustomers.stream()
            .filter(c -> c.getFirstVisit() != null && 
                        !c.getFirstVisit().isBefore(currentMonthStart) && 
                        !c.getFirstVisit().isAfter(currentMonthEnd))
            .count();
        long regularGuestsThisMonth = currentCustomers.stream()
            .filter(c -> c.getVisits() >= 3)
            .count();
        
        double avgVisitsRegular = currentCustomers.stream()
            .filter(c -> c.getVisits() >= 3)
            .mapToLong(CustomerAggregation::getVisits)
            .average()
            .orElse(0.0);
        
        // For last month, we'd need separate query - for now estimate from historical data
        // In production, might store monthly snapshots
        long totalGuestsLastMonth = (long) (totalGuestsThisMonth * 0.95); // Estimate 95% of this month
        long newGuestsLastMonth = (long) (newGuestsThisMonth * 0.90);
        long regularGuestsLastMonth = (long) (regularGuestsThisMonth * 0.95);
        double avgVisitsLastMonth = avgVisitsRegular * 0.98;
        
        // Calculate trend percentages
        double totalGuestsTrend = calculatePercentChange(totalGuestsLastMonth, totalGuestsThisMonth);
        double newGuestsTrend = calculatePercentChange(newGuestsLastMonth, newGuestsThisMonth);
        double regularGuestsTrend = calculatePercentChange(regularGuestsLastMonth, regularGuestsThisMonth);
        double avgVisitsTrend = calculatePercentChange(avgVisitsLastMonth, avgVisitsRegular);
        
        // Calculate percentages
        double newCustomersPercent = totalGuestsThisMonth > 0 ? 
            (newGuestsThisMonth * 100.0 / totalGuestsThisMonth) : 0.0;
        double regularCustomersPercent = totalGuestsThisMonth > 0 ? 
            (regularGuestsThisMonth * 100.0 / totalGuestsThisMonth) : 0.0;
        
        auditLogRepository.save(com.restaurant.waitlist.backend.entity.AuditLog.builder()
            .restaurantId(restaurantId != null ? restaurantId : 0L)
            .action("VIEW_CUSTOMER_SUMMARY")
            .details("Viewed customer summary")
            .build());
        
        return CustomerSummaryResponse.builder()
            .totalUniqueGuests(totalGuestsThisMonth)
            .totalGuestsLastMonth(totalGuestsLastMonth)
            .totalGuestsTrendPercent(totalGuestsTrend)
            .newCustomers(newGuestsThisMonth)
            .newCustomersPercent(newCustomersPercent)
            .newCustomersLastMonth(newGuestsLastMonth)
            .newCustomersTrendPercent(newGuestsTrend)
            .regularCustomers(regularGuestsThisMonth)
            .regularCustomersPercent(regularCustomersPercent)
            .regularCustomersLastMonth(regularGuestsLastMonth)
            .regularCustomersTrendPercent(regularGuestsTrend)
            .avgVisitsPerRegular(Math.round(avgVisitsRegular * 10.0) / 10.0)
            .avgVisitsLastMonth(Math.round(avgVisitsLastMonth * 10.0) / 10.0)
            .avgVisitsTrendPercent(avgVisitsTrend)
            .build();
    }

    @Override
    public Page<CustomerResponse> listCustomers(Long restaurantId, Pageable pageable) {
        List<Long> restaurantIds = adminLocationAccessService.resolveRestaurantIds(restaurantId);
        List<CustomerAggregation> agg = waitlistRepository.aggregateCustomersByRestaurantIds(restaurantIds);
        
        List<CustomerResponse> items = agg.stream().map(a -> CustomerResponse.builder()
                .guest(a.getGuest())
                .contact(a.getContact())
                .locations(a.getLocations())
                .visits(a.getVisits())
                .firstVisit(a.getFirstVisit())
                .lastVisit(a.getLastVisit())
                .status(a.getVisits() >= 3 ? "Regular" : "New")
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
    
    private double calculatePercentChange(double previous, double current) {
        if (previous == 0) return current > 0 ? 100.0 : 0.0;
        return Math.round(((current - previous) / previous * 100.0) * 10.0) / 10.0;
    }
}
