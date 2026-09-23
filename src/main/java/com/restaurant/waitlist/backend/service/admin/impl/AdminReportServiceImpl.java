package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.dto.request.admin.ReportScheduleRequest;
import com.restaurant.waitlist.backend.dto.response.ReportsResponse;
import com.restaurant.waitlist.backend.dto.response.admin.ReportResponse;
import com.restaurant.waitlist.backend.entity.ReportRecord;
import com.restaurant.waitlist.backend.enums.ReportType;
import com.restaurant.waitlist.backend.repository.ReportRepository;
import com.restaurant.waitlist.backend.repository.WaitlistRepository;
import com.restaurant.waitlist.backend.repository.FeedbackRepository;
import com.restaurant.waitlist.backend.repository.CampaignRepository;
import com.restaurant.waitlist.backend.repository.RedemptionRepository;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.service.AdminLocationAccessService;
import com.restaurant.waitlist.backend.service.AuditLogService;
import com.restaurant.waitlist.backend.service.admin.AdminReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminReportServiceImpl implements AdminReportService {

    private final WaitlistRepository waitlistRepository;
    private final FeedbackRepository feedbackRepository;
    private final CampaignRepository campaignRepository;
    private final RedemptionRepository redemptionRepository;
    private final RestaurantRepository restaurantRepository;
    private final ReportRepository reportRepository;
    private final AdminLocationAccessService adminLocationAccessService;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public ReportResponse generateReport(String type, Long locationId, String period, LocalDate startDate, LocalDate endDate) throws Exception {
        if (locationId != null) {
            adminLocationAccessService.assertAccess(locationId);
        }
        ReportType reportType = ReportType.fromValue(type);
        String normalizedType = reportType.getValue();
        LocalDate to = endDate != null ? endDate : LocalDate.now();
        LocalDate from = startDate != null ? startDate : (period != null && !period.isBlank() ? fromPeriod(period, to) : LocalDate.of(1970, 1, 1));
        Date fromDate = Date.valueOf(from);
        Date toDate = Date.valueOf(to);

        if ("location".equals(normalizedType) && locationId == null) {
            throw new IllegalArgumentException("locationId is required for location-scoped reports");
        }

        String scope = "overall".equals(normalizedType) ? "All locations" : "Single location";
        String locationName = null;

        long waitlistJoins;
        long seatedCount;
        Double avgWaitTime;
        long redemptionCount;
        long activeOfferCount;
        Double avgRating;
        long reviewCount;
        long totalUniqueGuests;

        if ("location".equals(normalizedType)) {
            if (locationId == null) {
                throw new IllegalArgumentException("locationId is required for location-scoped reports");
            }
            waitlistJoins = waitlistRepository.countByRestaurantInDateRange(locationId, fromDate, toDate);
            seatedCount = waitlistRepository.countByRestaurantAndStatusInDateRange(locationId, "SEATED", fromDate, toDate);
            avgWaitTime = waitlistRepository.averageSeatedDurationMinutes(locationId, fromDate, toDate);
            locationName = restaurantRepository.findById(locationId)
                    .map(com.restaurant.waitlist.backend.entity.Restaurant::getName)
                    .orElse("Location " + locationId);
            scope = locationName;
            redemptionCount = redemptionRepository.countRedemptionsByRestaurantAndDateRange(
                    locationId,
                    from.atStartOfDay(),
                    to.plusDays(1).atStartOfDay().minusNanos(1)
            );
            activeOfferCount = campaignRepository.countActiveCampaignsByRestaurant(locationId);
            avgRating = feedbackRepository.averageRatingByRestaurantIdAndDateRange(locationId, fromDate, toDate);
            reviewCount = feedbackRepository.countByWaitlistRestaurantIdAndDateRange(locationId, fromDate, toDate);
            totalUniqueGuests = waitlistRepository.aggregateCustomers(locationId, fromDate, toDate).stream().mapToLong(c -> 1L).sum();
        } else {
            // "overall" (and any other non-location-scoped report) covers the
            // reporting admin's whole franchise group, never every restaurant
            // in the system.
            List<Long> restaurantIds = adminLocationAccessService.getAccessibleRestaurantIds();
            waitlistJoins = restaurantIds.stream().mapToLong(id -> waitlistRepository.countByRestaurantInDateRange(id, fromDate, toDate)).sum();
            seatedCount = restaurantIds.stream().mapToLong(id -> waitlistRepository.countByRestaurantAndStatusInDateRange(id, "SEATED", fromDate, toDate)).sum();
            avgWaitTime = weightedAverageSeatedDuration(restaurantIds, fromDate, toDate);
            redemptionCount = restaurantIds.stream()
                    .mapToLong(id -> redemptionRepository.countRedemptionsByRestaurantAndDateRange(id, from.atStartOfDay(), to.plusDays(1).atStartOfDay().minusNanos(1)))
                    .sum();
            activeOfferCount = restaurantIds.stream().mapToLong(campaignRepository::countActiveCampaignsByRestaurant).sum();
            avgRating = weightedAverageRating(restaurantIds, fromDate, toDate);
            reviewCount = restaurantIds.stream().mapToLong(id -> feedbackRepository.countByWaitlistRestaurantIdAndDateRange(id, fromDate, toDate)).sum();
            totalUniqueGuests = waitlistRepository.aggregateCustomersByRestaurantIds(restaurantIds, fromDate, toDate).size();
        }

        if (avgRating == null) avgRating = 0.0;

        StringBuilder csvContent = new StringBuilder();
        String periodLabel = (startDate == null && endDate == null && (period == null || period.isBlank())) ? "All time" : from + " to " + to;

        csvContent.append("REPORT OVERVIEW\n");
        csvContent.append("Type,").append(normalizedType).append("\n");
        csvContent.append("Report Label,").append(reportType.getDisplayName()).append("\n");
        csvContent.append("Scope,").append(scope).append("\n");
        csvContent.append("Period,").append(periodLabel).append("\n");
        csvContent.append("Generated At,").append(LocalDateTime.now()).append("\n\n");

        csvContent.append("SUMMARY\n");
        csvContent.append("Metric,Value\n");
        csvContent.append("Waitlist Joins,").append(waitlistJoins).append("\n");
        csvContent.append("Guests Seated,").append(seatedCount).append("\n");
        csvContent.append("Average Wait Time (min),").append(avgWaitTime != null ? String.format("%.2f", avgWaitTime) : "0.00").append("\n");
        csvContent.append("Total Redemptions,").append(redemptionCount).append("\n");
        csvContent.append("Active Offers,").append(activeOfferCount).append("\n");
        csvContent.append("Total Reviews,").append(reviewCount).append("\n");
        csvContent.append("Average Rating,").append(String.format("%.2f", avgRating)).append("\n");
        csvContent.append("Unique Guests,").append(totalUniqueGuests).append("\n\n");

        csvContent.append("WAITLIST PERFORMANCE\n");
        csvContent.append("Metric,Value\n");
        csvContent.append("Join Count,").append(waitlistJoins).append("\n");
        csvContent.append("Seated Count,").append(seatedCount).append("\n");
        csvContent.append("Conversion Rate (%),").append(waitlistJoins > 0 ? String.format("%.2f", (seatedCount * 100.0) / waitlistJoins) : "0.00").append("\n");
        csvContent.append("Average Wait Time (min),").append(avgWaitTime != null ? String.format("%.2f", avgWaitTime) : "0.00").append("\n\n");

        csvContent.append("REDEMPTION & OFFER PERFORMANCE\n");
        csvContent.append("Metric,Value\n");
        csvContent.append("Redemption Count,").append(redemptionCount).append("\n");
        csvContent.append("Active Campaigns/Offers,").append(activeOfferCount).append("\n");
        csvContent.append("Campaign Reach,0\n\n");

        csvContent.append("REVIEWS & FEEDBACK\n");
        csvContent.append("Metric,Value\n");
        csvContent.append("Review Count,").append(reviewCount).append("\n");
        csvContent.append("Average Rating,").append(String.format("%.2f", avgRating)).append("\n");
        csvContent.append("Response Rate,0\n\n");

        csvContent.append("GUEST SEGMENT DATA\n");
        csvContent.append("Guest,Visits,Contact,Locations\n");
        java.util.List<com.restaurant.waitlist.backend.repository.CustomerAggregation> customers = "location".equals(normalizedType)
                ? waitlistRepository.aggregateCustomers(locationId, fromDate, toDate)
                : waitlistRepository.aggregateCustomersByRestaurantIds(adminLocationAccessService.getAccessibleRestaurantIds(), fromDate, toDate);
        if (customers != null && !customers.isEmpty()) {
            customers.stream().limit(10).forEach(customer -> {
                csvContent.append(customer.getGuest()).append(",");
                csvContent.append(customer.getVisits() != null ? customer.getVisits() : 0).append(",");
                csvContent.append(customer.getContact() != null ? customer.getContact() : "").append(",");
                csvContent.append(customer.getLocations() != null ? customer.getLocations() : "").append("\n");
            });
        } else {
            csvContent.append("No guest segment data available\n");
        }
        csvContent.append("\n");

        csvContent.append("LOCATION COMPARISON\n");
        csvContent.append("Location,Waitlist Joins\n");
        java.util.List<Object[]> topLocations = new java.util.ArrayList<>();
        if ("location".equals(normalizedType)) {
            topLocations.add(new Object[] { locationId, locationName, waitlistJoins });
        } else {
            topLocations.addAll(waitlistRepository.topRestaurantByJoinsForLocations(
                    adminLocationAccessService.getAccessibleRestaurantIds(), fromDate, toDate));
            topLocations.sort((a, b) -> Long.compare(
                    b[2] != null ? ((Number) b[2]).longValue() : 0L,
                    a[2] != null ? ((Number) a[2]).longValue() : 0L));
            if (topLocations.size() > 10) {
                topLocations = topLocations.subList(0, 10);
            }
        }
        if (!topLocations.isEmpty()) {
            for (Object[] row : topLocations) {
                Long restaurantId = row[0] != null ? ((Number) row[0]).longValue() : null;
                String name = row[1] != null ? row[1].toString() : (restaurantId != null ? "Location " + restaurantId : "Unknown");
                Long joins = row[2] != null ? ((Number) row[2]).longValue() : 0L;
                csvContent.append(name).append(",").append(joins).append("\n");
            }
        } else {
            csvContent.append("No comparison data available\n");
        }

        String fileName = String.format("report_%s_%s_%s.csv",
                normalizedType,
                from,
                System.currentTimeMillis());

        File dir = new File("target/reports");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(csvContent.toString().getBytes());
        }

        ReportRecord rec = ReportRecord.builder()
                .fileName(fileName)
                .filePath(file.getAbsolutePath())
                .type(normalizedType)
                .locationId("location".equals(normalizedType) ? locationId : null)
                .generatedByRestaurantId(adminLocationAccessService.getCurrentAdminRestaurantId())
                .period(period)
                .build();
        ReportRecord saved = reportRepository.save(rec);

        auditLogService.log(locationId != null ? locationId : 0L, "GENERATE_REPORT",
                "Generated " + normalizedType + " report from " + from + " to " + to);

        return ReportResponse.builder()
                .id(saved.getId())
                .fileName(saved.getFileName())
                .type(saved.getType())
                .scope(scope)
                .locationId("location".equals(normalizedType) ? locationId : null)
                .locationName(locationName)
                .period(period)
                .dateRange(from + " to " + to)
                .generatedAt(saved.getGeneratedAt())
                .build();
    }

    @Override
    public Page<ReportResponse> listReports(Pageable pageable) {
        List<Long> restaurantIds = adminLocationAccessService.getAccessibleRestaurantIds();
        Page<ReportRecord> page = reportRepository.findByGeneratedByRestaurantIdIn(restaurantIds, pageable);
        List<ReportResponse> items = page.getContent().stream().map(r -> {
            String scope = "overall".equals(r.getType()) ? "All locations" : (r.getLocationId() != null ? "Location " + r.getLocationId() : "All locations");
            return ReportResponse.builder()
                    .id(r.getId())
                    .fileName(r.getFileName())
                    .type(r.getType())
                    .scope(scope)
                    .locationId(r.getLocationId())
                    .period(r.getPeriod())
                    .generatedAt(r.getGeneratedAt())
                    .build();
        }).collect(Collectors.toList());

        auditLogService.log(0L, "LIST_REPORTS", "Listed reports, pageSize=" + pageable.getPageSize());

        return new PageImpl<>(items, pageable, page.getTotalElements());
    }

    @Override
    public byte[] downloadReport(Long reportId) throws Exception {
        ReportRecord r = reportRepository.findById(reportId).orElseThrow(() -> new IllegalArgumentException("Report not found"));
        adminLocationAccessService.assertAccess(r.getGeneratedByRestaurantId());
        File f = new File(r.getFilePath());

        auditLogService.log(r.getLocationId() != null ? r.getLocationId() : 0L, "DOWNLOAD_REPORT", "Downloaded report: " + r.getFileName());
        return java.nio.file.Files.readAllBytes(f.toPath());
    }

    @Override
    public ReportResponse getReportMetadata(Long reportId) throws Exception {
        ReportRecord r = reportRepository.findById(reportId).orElseThrow(() -> new IllegalArgumentException("Report not found"));
        adminLocationAccessService.assertAccess(r.getGeneratedByRestaurantId());
        String scope = "overall".equals(r.getType()) ? "All locations" : (r.getLocationId() != null ? "Location " + r.getLocationId() : "All locations");
        return ReportResponse.builder()
            .id(r.getId())
            .fileName(r.getFileName())
            .type(r.getType())
            .scope(scope)
            .locationId(r.getLocationId())
            .period(r.getPeriod())
            .generatedAt(r.getGeneratedAt())
            .build();
    }

    @Override
    @Transactional
    public java.util.Map<String, Object> scheduleReport(com.restaurant.waitlist.backend.dto.request.admin.ReportScheduleRequest request) {
        adminLocationAccessService.assertAccess(request.getLocationId());
        // Create scheduled report record
        java.util.Map<String, Object> scheduled = new java.util.HashMap<>();
        scheduled.put("id", System.currentTimeMillis());
        scheduled.put("name", request.getName());
        scheduled.put("type", request.getType());
        scheduled.put("locationId", request.getLocationId());
        scheduled.put("frequency", request.getFrequency());
        scheduled.put("scheduleTime", request.getScheduleTime());
        scheduled.put("emailRecipients", request.getEmailRecipients());
        scheduled.put("exportFormat", request.getExportFormat());
        scheduled.put("createdAt", System.currentTimeMillis());

        auditLogService.log(request.getLocationId() != null ? request.getLocationId() : 0L, "SCHEDULE_REPORT",
                "Scheduled report: " + request.getName() + " frequency=" + request.getFrequency());

        return scheduled;
    }

    @Override
    public Page<java.util.Map<String, Object>> listScheduledReports(Pageable pageable) {
        // Simplified - would normally query a ScheduledReport entity
        List<java.util.Map<String, Object>> scheduled = new java.util.ArrayList<>();
        scheduled.add(java.util.Map.of(
            "id", 1L,
            "name", "Weekly Performance Report",
            "type", "performance",
            "frequency", "WEEKLY",
            "scheduleTime", "Monday 9:00 AM",
            "emailRecipients", java.util.List.of("admin@restaurant.com")
        ));
        
        return new PageImpl<>(scheduled, pageable, scheduled.size());
    }

    @Override
    @Transactional
    public void cancelScheduledReport(Long scheduleId) {
        auditLogService.log(0L, "CANCEL_SCHEDULED_REPORT", "Cancelled scheduled report: " + scheduleId);
    }

    private Double weightedAverageSeatedDuration(List<Long> restaurantIds, Date fromDate, Date toDate) {
        long totalWeight = 0;
        double totalSum = 0;
        for (Long id : restaurantIds) {
            long weight = waitlistRepository.countByRestaurantAndStatusInDateRange(id, "SEATED", fromDate, toDate);
            Double avg = waitlistRepository.averageSeatedDurationMinutes(id, fromDate, toDate);
            if (weight > 0 && avg != null) {
                totalSum += avg * weight;
                totalWeight += weight;
            }
        }
        return totalWeight > 0 ? totalSum / totalWeight : 0.0;
    }

    private Double weightedAverageRating(List<Long> restaurantIds, Date fromDate, Date toDate) {
        long totalWeight = 0;
        double totalSum = 0;
        for (Long id : restaurantIds) {
            long weight = feedbackRepository.countByWaitlistRestaurantIdAndDateRange(id, fromDate, toDate);
            Double avg = feedbackRepository.averageRatingByRestaurantIdAndDateRange(id, fromDate, toDate);
            if (weight > 0 && avg != null) {
                totalSum += avg * weight;
                totalWeight += weight;
            }
        }
        return totalWeight > 0 ? totalSum / totalWeight : 0.0;
    }

    /**
     * Convert period string to start date for date range.
     */
    private LocalDate fromPeriod(String period, LocalDate to) {
        if (period == null || period.isBlank()) {
            return LocalDate.of(1970, 1, 1);
        }
        switch (period.trim().toLowerCase()) {
            case "pastweek":
            case "last7days":
                return to.minusDays(7);
            case "pastmonth":
            case "last30days":
            case "lastmonth":
                return to.minusMonths(1);
            case "last3months":
                return to.minusMonths(3);
            default:
                return LocalDate.of(1970, 1, 1);
        }
    }
}
