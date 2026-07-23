package com.restaurant.waitlist.backend.controller;

import com.restaurant.waitlist.backend.dto.request.UpdateRestaurantSettingsRequest;
import com.restaurant.waitlist.backend.dto.response.*;
import com.restaurant.waitlist.backend.service.AdminService;
import com.restaurant.waitlist.backend.dto.request.UpdateSmsTemplateRequest;
import com.restaurant.waitlist.backend.service.RestaurantService;
import com.restaurant.waitlist.backend.service.SmsTemplateService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private SmsTemplateService smsTemplateService;

    @Autowired
    private RestaurantService restaurantService;

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    @GetMapping("/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getAnalytics() {
        try {
            log.info("START: getAnalytics | {}", "");
            AnalyticsResponse response = adminService.getAnalytics();
            log.info("END: getAnalytics | success");
            return ResponseEntity.ok(ApiResponse.success("Analytics data retrieved", response));
        } catch (Exception e) {
            log.error("ERROR: getAnalytics | {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/guests")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<GuestHistoryResponse>>> getGuestHistory() {
        try {
            log.info("START: getGuestHistory | {}", "");
            List<GuestHistoryResponse> response = adminService.getGuestHistory();
            log.info("END: getGuestHistory | success");
            return ResponseEntity.ok(ApiResponse.success("Guest history retrieved", response));
        } catch (Exception e) {
            log.error("ERROR: getGuestHistory | {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/feedback")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FeedbackInsightsResponse>> getFeedbackInsights() {
        try {
            log.info("START: getFeedbackInsights | {}", "");
            FeedbackInsightsResponse response = adminService.getFeedbackInsights();
            log.info("END: getFeedbackInsights | success");
            return ResponseEntity.ok(ApiResponse.success("Feedback insights retrieved", response));
        } catch (Exception e) {
            log.error("ERROR: getFeedbackInsights | {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/sms-templates")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SmsTemplateResponse>>> getSmsTemplates() {
        try {
            log.info("START: getSmsTemplates | {}", "");
            List<SmsTemplateResponse> response = smsTemplateService.getAllTemplates().stream()
                    .map(SmsTemplateResponse::fromSmsTemplate)
                    .collect(Collectors.toList());
            log.info("END: getSmsTemplates | success");
            return ResponseEntity.ok(ApiResponse.success("SMS templates retrieved", response));
        } catch (Exception e) {
            log.error("ERROR: getSmsTemplates | {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/twilio-test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<java.util.Map<String, String>>> twilioTest() {
        try {
            log.info("START: twilioTest | {}", "");
            java.util.Map<String, String> response = adminService.getTwilioProbe();
            log.info("END: twilioTest | success");
            return ResponseEntity.ok(ApiResponse.success("Twilio probe result", response));
        } catch (Exception e) {
            log.error("ERROR: twilioTest | {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
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
    @GetMapping("/{restaurantId}/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReportsResponse>> getReports(@PathVariable Long restaurantId,
                                                                   @RequestParam(required = false) String fromDate,
                                                                   @RequestParam(required = false) String toDate) {
        try {
            log.info("START: getReports | restaurantId={}, fromDate={}, toDate={}", restaurantId, fromDate, toDate);
            ReportsResponse response = adminService.getReports(restaurantId, fromDate, toDate);
            log.info("END: getReports | success");
            return ResponseEntity.ok(ApiResponse.success("Reports retrieved", response));
        } catch (Exception e) {
            log.error("ERROR: getReports | {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }


}

