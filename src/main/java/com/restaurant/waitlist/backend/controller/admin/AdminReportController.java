package com.restaurant.waitlist.backend.controller.admin;

import com.restaurant.waitlist.backend.dto.request.admin.ReportScheduleRequest;
import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.dto.response.admin.ReportResponse;
import com.restaurant.waitlist.backend.service.admin.AdminReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService adminReportService;

    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReportResponse> generate(@RequestParam(required = false) String type,
                                                   @RequestParam(required = false) Long locationId,
                                                   @RequestParam(required = false, defaultValue = "last30days") String period,
                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws Exception {
        ReportResponse resp = adminReportService.generateReport(type, locationId, period, startDate, endDate);
        return ResponseEntity.ok(resp);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ReportResponse>> list(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        Page<ReportResponse> p = adminReportService.listReports(PageRequest.of(page, size));
        return ResponseEntity.ok(p);
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> download(@PathVariable Long id) throws Exception {
        byte[] data = adminReportService.downloadReport(id);
        com.restaurant.waitlist.backend.dto.response.admin.ReportResponse meta = adminReportService.getReportMetadata(id);
        String filename = meta != null && meta.getFileName() != null ? meta.getFileName() : "report.csv";
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(data);
    }

    @PostMapping("/{id}/export/excel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportAsExcel(@PathVariable Long id) throws Exception {
        byte[] data = adminReportService.exportReportAsExcel(id);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.xlsx")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(data);
    }

    @PostMapping("/{id}/export/pdf")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportAsPdf(@PathVariable Long id) throws Exception {
        byte[] data = adminReportService.exportReportAsPdf(id);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(data);
    }

    @PostMapping("/{id}/export/csv")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportAsCsv(@PathVariable Long id) throws Exception {
        byte[] data = adminReportService.exportReportCsv(id);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.csv")
            .contentType(MediaType.TEXT_PLAIN)
            .body(data);
    }

    @PostMapping("/schedule")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> scheduleReport(@Valid @RequestBody ReportScheduleRequest request) {
        Map<String, Object> scheduled = adminReportService.scheduleReport(request);
        return ResponseEntity.ok(ApiResponse.success("Report scheduled successfully", scheduled));
    }

    @GetMapping("/scheduled")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> listScheduledReports(@RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success("Scheduled reports retrieved", 
            adminReportService.listScheduledReports(pageable)));
    }

    @DeleteMapping("/schedule/{scheduleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> cancelScheduledReport(@PathVariable Long scheduleId) {
        adminReportService.cancelScheduledReport(scheduleId);
        return ResponseEntity.ok(ApiResponse.success("Scheduled report cancelled", null));
    }
}
