package com.restaurant.waitlist.backend.controller;

import com.restaurant.waitlist.backend.dto.request.UpdateRestaurantSettingsRequest;
import com.restaurant.waitlist.backend.dto.response.*;
import com.restaurant.waitlist.backend.service.AdminService;
import com.restaurant.waitlist.backend.dto.request.UpdateSmsTemplateRequest;
import com.restaurant.waitlist.backend.service.RestaurantService;
import com.restaurant.waitlist.backend.service.SmsTemplateService;
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
    @GetMapping("/{restaurantId}/guest-history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<WaitlistResponse>>> getGuestHistory(@PathVariable Long restaurantId,
                                                                               @RequestParam(defaultValue = "0") int page,
                                                                               @RequestParam(defaultValue = "10") int size,
                                                                               @RequestParam(required = false) String status,
                                                                               @RequestParam(required = false) String date) {
        try {
            log.info("START: getGuestHistory | restaurantId={}, page={}, size={}, status={}, date={}", restaurantId, page, size, status, date);

            if (page < 0 || size <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid page or size"));
            }

            Page<WaitlistResponse> response = adminService.getGuestHistory(restaurantId, page, size, status, date);
            log.info("END: getGuestHistory | success");
            return ResponseEntity.ok(ApiResponse.success("Guest history retrieved", response));
        } catch (Exception e) {
            log.error("ERROR: getGuestHistory | {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{restaurantId}/guest-history/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> exportGuestHistory(@PathVariable Long restaurantId,
                                                @RequestParam(required = false) String status,
                                                @RequestParam(required = false) String date) {
        try {
            log.info("START: exportGuestHistory | restaurantId={}, status={}, date={}", restaurantId, status, date);
            String csv = adminService.exportGuestHistoryCsv(restaurantId, status, date);
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.add("Content-Type", "text/csv; charset=utf-8");
            headers.add("Content-Disposition", "attachment; filename=\"guest-history.csv\"");
            log.info("END: exportGuestHistory | success");
            return ResponseEntity.ok().headers(headers).body(csv);
        } catch (Exception e) {
            log.error("ERROR: exportGuestHistory | {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
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

    @GetMapping("/{restaurantId}/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RestaurantSettingsResponse>> getSettings(@PathVariable Long restaurantId) {
        try {
            log.info("START: getSettings | restaurantId={}", restaurantId);
            RestaurantSettingsResponse response = adminService.getSettings(restaurantId);
            log.info("END: getSettings | success");
            return ResponseEntity.ok(ApiResponse.success("Settings retrieved", response));
        } catch (Exception e) {
            log.error("ERROR: getSettings | {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{restaurantId}/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RestaurantSettingsResponse>> updateSettings(@PathVariable Long restaurantId,
                                                                                  @Valid @RequestBody UpdateRestaurantSettingsRequest request) {
        try {
            log.info("START: updateSettings | restaurantId={}, request={}", restaurantId, request);
            RestaurantSettingsResponse response = adminService.updateSettings(restaurantId, request);
            log.info("END: updateSettings | success");
            return ResponseEntity.ok(ApiResponse.success("Settings updated successfully", response));
        } catch (Exception e) {
            log.error("ERROR: updateSettings | {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}

