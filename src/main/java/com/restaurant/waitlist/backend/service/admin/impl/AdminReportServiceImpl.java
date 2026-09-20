package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.dto.request.admin.ReportScheduleRequest;
import com.restaurant.waitlist.backend.dto.response.ReportsResponse;
import com.restaurant.waitlist.backend.dto.response.admin.ReportResponse;
import com.restaurant.waitlist.backend.entity.ReportRecord;
import com.restaurant.waitlist.backend.repository.ReportRepository;
import com.restaurant.waitlist.backend.repository.WaitlistRepository;
import com.restaurant.waitlist.backend.repository.FeedbackRepository;
import com.restaurant.waitlist.backend.repository.CampaignRepository;
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
    private final ReportRepository reportRepository;
    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public ReportResponse generateReport(String type, Long locationId, String period, LocalDate startDate, LocalDate endDate) throws Exception {
        // Determine date range - custom dates take precedence
        LocalDate to = endDate != null ? endDate : LocalDate.now();
        LocalDate from = startDate != null ? startDate : fromPeriod(period, to);
        Date fromDate = Date.valueOf(from);
        Date toDate = Date.valueOf(to);

        // Validate scope
        if ("location".equals(type) && locationId == null) {
            throw new IllegalArgumentException("locationId is required for location-scoped reports");
        }

        // Build report data
        StringBuilder csvContent = new StringBuilder();
        String scope = "overall".equals(type) ? "All locations" : "Single location";
        String locationName = null;

        // Query metrics based on scope
        long waitlistJoins;
        long seatedCount;
        Double avgWaitTime;
        long redemptionCount;
        long activeOfferCount;
        Double avgRating;
        long reviewCount;

        if ("location".equals(type)) {
            // Location-scoped report
            waitlistJoins = waitlistRepository.countByRestaurantInDateRange(locationId, fromDate, toDate);
            seatedCount = waitlistRepository.countByRestaurantAndStatusInDateRange(locationId, "SEATED", fromDate, toDate);
            avgWaitTime = waitlistRepository.averageSeatedDurationMinutes(locationId, fromDate, toDate);
            
            // Set location name (can be enhanced to query actual name from Restaurant entity)
            locationName = "Location " + locationId;
            scope = locationName;
        } else {
            // Overall report - all locations
            waitlistJoins = waitlistRepository.countAllInDateRange(fromDate, toDate);
            seatedCount = waitlistRepository.countByRestaurantAndStatusInDateRange(0L, "SEATED", fromDate, toDate);
            avgWaitTime = waitlistRepository.averageSeatedDurationMinutes(null, fromDate, toDate);
        }

        // Query redemptions (all locations or scoped)
        try {
            // This would need a custom query in repository to filter by location and date range
            redemptionCount = 0L; // Placeholder - needs proper redemption repo query
        } catch (Exception e) {
            redemptionCount = 0L;
        }

        // Query offers
        try {
            activeOfferCount = 0L; // Placeholder - needs campaign repo query
        } catch (Exception e) {
            activeOfferCount = 0L;
        }

        // Query reviews and ratings
        try {
            if ("location".equals(type)) {
                avgRating = feedbackRepository.averageRatingByRestaurantIdAndDateRange(locationId, fromDate, toDate);
                reviewCount = feedbackRepository.countByWaitlistRestaurantIdAndDateRange(locationId, fromDate, toDate);
            } else {
                avgRating = feedbackRepository.averageRatingByDateRange(fromDate, toDate);
                reviewCount = feedbackRepository.countByDateRange(fromDate, toDate);
            }
        } catch (Exception e) {
            avgRating = 0.0;
            reviewCount = 0L;
        }

        // Build CSV header
        csvContent.append("REPORT DATA\n");
        csvContent.append("Scope,").append(scope).append("\n");
        csvContent.append("Period,").append(from).append(" to ").append(to).append("\n");
        csvContent.append("Generated,").append(LocalDateTime.now()).append("\n\n");

        // Add metrics section
        csvContent.append("WAITLIST METRICS\n");
        csvContent.append("Metric,Value\n");
        csvContent.append("Waitlist Joins,").append(waitlistJoins).append("\n");
        csvContent.append("Guests Seated,").append(seatedCount).append("\n");
        csvContent.append("Average Wait Time (minutes),").append(avgWaitTime != null ? String.format("%.2f", avgWaitTime) : "0").append("\n\n");

        // Redemptions section
        csvContent.append("REDEMPTIONS\n");
        csvContent.append("Metric,Value\n");
        csvContent.append("Total Redemptions,").append(redemptionCount).append("\n\n");

        // Offers section
        csvContent.append("OFFERS\n");
        csvContent.append("Metric,Value\n");
        csvContent.append("Active Offers,").append(activeOfferCount).append("\n\n");

        // Reviews section
        csvContent.append("REVIEWS\n");
        csvContent.append("Metric,Value\n");
        csvContent.append("Total Reviews,").append(reviewCount).append("\n");
        csvContent.append("Average Rating,").append(avgRating != null ? String.format("%.2f", avgRating) : "0").append("\n");

        // Write CSV file
        String fileName = String.format("report_%s_%s_%s.csv", 
            "overall".equals(type) ? "overall" : "location", 
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

        // Persist report record
        ReportRecord rec = ReportRecord.builder()
                .fileName(fileName)
                .filePath(file.getAbsolutePath())
                .type(type)
                .locationId("location".equals(type) ? locationId : null)
                .period(period)
                .build();
        ReportRecord saved = reportRepository.save(rec);

        // Audit log
        auditLogRepository.save(com.restaurant.waitlist.backend.entity.AuditLog.builder()
            .restaurantId(locationId != null ? locationId : 0L)
            .action("GENERATE_REPORT")
            .details("Generated " + type + " report from " + from + " to " + to)
            .build());

        // Build response
        return ReportResponse.builder()
                .id(saved.getId())
                .fileName(saved.getFileName())
                .type(saved.getType())
                .scope(scope)
                .locationId("location".equals(type) ? locationId : null)
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
        if (period == null) return to.minusDays(30);
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
                return to.minusDays(30);
        }
    }
}
