package com.restaurant.waitlist.backend.controller.admin;

import com.restaurant.waitlist.backend.dto.response.admin.ReportResponse;
import com.restaurant.waitlist.backend.service.admin.AdminReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService adminReportService;

    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReportResponse> generate(@RequestParam(required = false) String type,
                                                   @RequestParam(required = false) Long locationId,
                                                   @RequestParam(required = false, defaultValue = "last30days") String period) throws Exception {
        ReportResponse resp = adminReportService.generateReport(type, locationId, period);
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
}
