package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.dto.response.ReportsResponse;
import com.restaurant.waitlist.backend.dto.response.admin.ReportResponse;
import com.restaurant.waitlist.backend.entity.ReportRecord;
import com.restaurant.waitlist.backend.repository.ReportRepository;
import com.restaurant.waitlist.backend.repository.WaitlistRepository;
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
    private final ReportRepository reportRepository;
    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public ReportResponse generateReport(String type, Long locationId, String period) throws Exception {
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusMonths(1);
        if ("last7days".equals(period)) from = to.minusDays(7);
        else if ("last3months".equals(period)) from = to.minusMonths(3);

        Date fromDate = Date.valueOf(from);
        Date toDate = Date.valueOf(to);

        ReportsResponse summary = ReportsResponse.builder()
                .totalGuests(waitlistRepository.countAllInDateRange(fromDate, toDate))
                .totalSeated(waitlistRepository.countAllInDateRange(fromDate, toDate))
                .averageWaitTime(0)
                .todayGuestsCount(0)
                .todaySeatedCount(0)
                .totalWaiting(0)
                .totalNotified(0)
                .totalCancelled(0)
                .build();

        // generate simple CSV file containing the summary
        String fileName = String.format("report_%s_%s.csv", type != null ? type : "overall", System.currentTimeMillis());
        File dir = new File("target/reports");
        dir.mkdirs();
        File file = new File(dir, fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            String header = "metric,value\n";
            fos.write(header.getBytes());
            fos.write(("totalGuests," + summary.getTotalGuests() + "\n").getBytes());
            fos.write(("totalSeated," + summary.getTotalSeated() + "\n").getBytes());
        }

        ReportRecord rec = ReportRecord.builder()
                .fileName(fileName)
                .filePath(file.getAbsolutePath())
                .type(type)
                .locationId(locationId)
                .period(period)
                .build();
        ReportRecord saved = reportRepository.save(rec);

        // audit
        auditLogRepository.save(com.restaurant.waitlist.backend.entity.AuditLog.builder()
            .restaurantId(locationId != null ? locationId : 0L)
            .action("GENERATE_REPORT")
            .details("Generated report: " + saved.getFileName() + " type=" + type + " period=" + period)
            .build());

        return ReportResponse.builder()
                .id(saved.getId())
                .fileName(saved.getFileName())
                .type(saved.getType())
                .locationId(saved.getLocationId())
                .period(saved.getPeriod())
                .generatedAt(saved.getGeneratedAt())
                .build();
    }

    @Override
    public Page<ReportResponse> listReports(Pageable pageable) {
        Page<ReportRecord> page = reportRepository.findAll(pageable);
        List<ReportResponse> items = page.getContent().stream().map(r -> ReportResponse.builder()
                .id(r.getId())
                .fileName(r.getFileName())
                .type(r.getType())
                .locationId(r.getLocationId())
                .period(r.getPeriod())
                .generatedAt(r.getGeneratedAt())
                .build()).collect(Collectors.toList());
        // audit listing
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
        return ReportResponse.builder()
            .id(r.getId())
            .fileName(r.getFileName())
            .type(r.getType())
            .locationId(r.getLocationId())
            .period(r.getPeriod())
            .generatedAt(r.getGeneratedAt())
            .build();
        }
}
