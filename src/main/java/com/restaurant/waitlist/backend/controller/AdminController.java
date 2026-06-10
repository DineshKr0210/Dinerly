package com.restaurant.waitlist.backend.controller;

import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.dto.response.AnalyticsResponse;
import com.restaurant.waitlist.backend.dto.response.FeedbackInsightsResponse;
import com.restaurant.waitlist.backend.dto.response.GuestHistoryResponse;
import com.restaurant.waitlist.backend.service.AdminService;
import com.restaurant.waitlist.backend.dto.request.UpdateSmsTemplateRequest;
import com.restaurant.waitlist.backend.dto.response.SmsTemplateResponse;
import com.restaurant.waitlist.backend.service.SmsTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private SmsTemplateService smsTemplateService;

    @GetMapping("/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getAnalytics() {
        try {
            AnalyticsResponse response = adminService.getAnalytics();
            return ResponseEntity.ok(ApiResponse.success("Analytics data retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/guests")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<GuestHistoryResponse>>> getGuestHistory() {
        try {
            List<GuestHistoryResponse> response = adminService.getGuestHistory();
            return ResponseEntity.ok(ApiResponse.success("Guest history retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/feedback")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FeedbackInsightsResponse>> getFeedbackInsights() {
        try {
            FeedbackInsightsResponse response = adminService.getFeedbackInsights();
            return ResponseEntity.ok(ApiResponse.success("Feedback insights retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/sms-templates")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SmsTemplateResponse>>> getSmsTemplates() {
        try {
            List<SmsTemplateResponse> response = smsTemplateService.getAllTemplates().stream()
                    .map(SmsTemplateResponse::fromSmsTemplate)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success("SMS templates retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/sms-templates/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SmsTemplateResponse>> updateSmsTemplate(
            @PathVariable Long id,
            @RequestBody UpdateSmsTemplateRequest request) {
        try {
            com.restaurant.waitlist.backend.entity.SmsTemplate template = smsTemplateService.updateTemplate(id, request.getMessageTemplate(), request.getDescription());
            return ResponseEntity.ok(ApiResponse.success("SMS template updated successfully", SmsTemplateResponse.fromSmsTemplate(template)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}

