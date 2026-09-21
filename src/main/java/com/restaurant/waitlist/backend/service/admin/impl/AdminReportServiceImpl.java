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
import com.restaurant.waitlist.backend.service.admin.AdminReportService;
import com.restaurant.waitlist.backend.repository.AuditLogRepository;
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
    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public ReportResponse generateReport(String type, Long locationId, String period, LocalDate startDate, LocalDate endDate) throws Exception {
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
            waitlistJoins = waitlistRepository.countAllInDateRange(fromDate, toDate);
            seatedCount = waitlistRepository.countByRestaurantAndStatusInDateRange(null, "SEATED", fromDate, toDate);
            avgWaitTime = waitlistRepository.averageSeatedDurationMinutes(null, fromDate, toDate);
            redemptionCount = redemptionRepository.countRedemptionsByDateRange(
                    from.atStartOfDay(),
                    to.plusDays(1).atStartOfDay().minusNanos(1)
            );
            activeOfferCount = campaignRepository.countActiveCampaigns();
            avgRating = feedbackRepository.averageRatingByDateRange(fromDate, toDate);
            reviewCount = feedbackRepository.countByDateRange(fromDate, toDate);
            totalUniqueGuests = waitlistRepository.aggregateCustomers(null, fromDate, toDate).stream().mapToLong(c -> 1L).sum();
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
        java.util.List<com.restaurant.waitlist.backend.repository.CustomerAggregation> customers = waitlistRepository.aggregateCustomers(locationId, fromDate, toDate);
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
            topLocations.addAll(waitlistRepository.topRestaurantsByJoins(fromDate, toDate, 10));
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
                .period(period)
                .build();
        ReportRecord saved = reportRepository.save(rec);

        auditLogRepository.save(com.restaurant.waitlist.backend.entity.AuditLog.builder()
                .restaurantId(locationId != null ? locationId : 0L)
                .action("GENERATE_REPORT")
                .details("Generated " + normalizedType + " report from " + from + " to " + to)
                .build());

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
        Page<ReportRecord> page = reportRepository.findAll(pageable);
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
        
        auditLogRepository.save(com.restaurant.waitlist.backend.entity.AuditLog.builder()
            .restaurantId(0L)
            .action("LIST_REPORTS")
            .details("Listed reports, pageSize=" + pageable.getPageSize())
            .build());
        
        return new PageImpl<>(items, pageable, page.getTotalElements());
    }

    @Override
    public byte[] downloadReport(Long reportId) throws Exception {
        ReportRecord r = reportRepository.findById(reportId).orElseThrow(() -> new IllegalArgumentException("Report not found"));
        File f = new File(r.getFilePath());

        auditLogRepository.save(com.restaurant.waitlist.backend.entity.AuditLog.builder()
            .restaurantId(r.getLocationId() != null ? r.getLocationId() : 0L)
            .action("DOWNLOAD_REPORT")
            .details("Downloaded report: " + r.getFileName())
            .build());
        return java.nio.file.Files.readAllBytes(f.toPath());
    }

    @Override
    public ReportResponse getReportMetadata(Long reportId) throws Exception {
        ReportRecord r = reportRepository.findById(reportId).orElseThrow(() -> new IllegalArgumentException("Report not found"));
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
    public byte[] exportReportAsExcel(Long reportId) throws Exception {
        ReportRecord r = reportRepository.findById(reportId).orElseThrow(() -> new RuntimeException("Report not found"));
        
        // Placeholder - Excel export not in requirements
        // In production, use POI (Apache POI) to generate real XLSX files
        String excelContent = "Report ID,Type,Location,Period,Generated\n";
        excelContent += r.getId() + "," + r.getType() + "," + r.getLocationId() + "," + r.getPeriod() + "," + r.getGeneratedAt() + "\n";
        
        auditLogRepository.save(com.restaurant.waitlist.backend.entity.AuditLog.builder()
            .restaurantId(r.getLocationId() != null ? r.getLocationId() : 0L)
            .action("EXPORT_EXCEL")
            .details("Exported report as Excel: " + r.getFileName())
            .build());
        
        return excelContent.getBytes();
    }

    @Override
    public byte[] exportReportAsPdf(Long reportId) throws Exception {
        ReportRecord r = reportRepository.findById(reportId).orElseThrow(() -> new RuntimeException("Report not found"));
        
        // Placeholder - PDF export not in requirements
        // In production, use iText or similar to generate real PDF files
        String pdfContent = "PDF Report\nReport ID: " + r.getId() + "\nType: " + r.getType() + "\nPeriod: " + r.getPeriod();
        
        auditLogRepository.save(com.restaurant.waitlist.backend.entity.AuditLog.builder()
            .restaurantId(r.getLocationId() != null ? r.getLocationId() : 0L)
            .action("EXPORT_PDF")
            .details("Exported report as PDF: " + r.getFileName())
            .build());
        
        return pdfContent.getBytes();
    }

    @Override
    public byte[] exportReportCsv(Long reportId) throws Exception {
        ReportRecord r = reportRepository.findById(reportId).orElseThrow(() -> new RuntimeException("Report not found"));
        
        String csvContent = "metric,value\n";
        csvContent += "Report ID," + r.getId() + "\n";
        csvContent += "Type," + r.getType() + "\n";
        csvContent += "Period," + r.getPeriod() + "\n";
        csvContent += "Generated At," + r.getGeneratedAt() + "\n";
        
        auditLogRepository.save(com.restaurant.waitlist.backend.entity.AuditLog.builder()
            .restaurantId(r.getLocationId() != null ? r.getLocationId() : 0L)
            .action("EXPORT_CSV")
            .details("Exported report as CSV: " + r.getFileName())
            .build());
        
        return csvContent.getBytes();
    }

    @Override
    @Transactional
    public java.util.Map<String, Object> scheduleReport(com.restaurant.waitlist.backend.dto.request.admin.ReportScheduleRequest request) {
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
        
        auditLogRepository.save(com.restaurant.waitlist.backend.entity.AuditLog.builder()
            .restaurantId(request.getLocationId() != null ? request.getLocationId() : 0L)
            .action("SCHEDULE_REPORT")
            .details("Scheduled report: " + request.getName() + " frequency=" + request.getFrequency())
            .build());
        
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
        auditLogRepository.save(com.restaurant.waitlist.backend.entity.AuditLog.builder()
            .restaurantId(0L)
            .action("CANCEL_SCHEDULED_REPORT")
            .details("Cancelled scheduled report: " + scheduleId)
            .build());
    }

    @Override
    public Page<java.util.Map<String, Object>> listCustomReportTemplates(Pageable pageable) {
        // Simplified - would normally query a ReportTemplate entity
        List<java.util.Map<String, Object>> templates = new java.util.ArrayList<>();
        templates.add(java.util.Map.of(
            "id", 1L,
            "name", "Daily Summary",
            "type", "summary",
            "fields", java.util.List.of("guests", "seated", "waitTime"),
            "createdAt", System.currentTimeMillis()
        ));
        
        return new PageImpl<>(templates, pageable, templates.size());
    }

    @Override
    @Transactional
    public java.util.Map<String, Object> createCustomReportTemplate(java.util.Map<String, Object> templateConfig) {
        // Create custom report template
        java.util.Map<String, Object> template = new java.util.HashMap<>(templateConfig);
        template.put("id", System.currentTimeMillis());
        template.put("createdAt", System.currentTimeMillis());
        
        auditLogRepository.save(com.restaurant.waitlist.backend.entity.AuditLog.builder()
            .restaurantId(0L)
            .action("CREATE_CUSTOM_TEMPLATE")
            .details("Created custom report template")
            .build());
        
        return template;
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
