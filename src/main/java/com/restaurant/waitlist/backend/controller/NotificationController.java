package com.restaurant.waitlist.backend.controller;

import com.restaurant.waitlist.backend.dto.request.SendSmsRequest;
import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.dto.response.NotificationSummaryResponse;
import com.restaurant.waitlist.backend.dto.response.SendCallResponse;
import com.restaurant.waitlist.backend.dto.response.SendSmsResponse;
import com.restaurant.waitlist.backend.dto.response.SmsHistoryResponse;
import com.restaurant.waitlist.backend.dto.response.WaitlistResponse;
import com.restaurant.waitlist.backend.service.NotificationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurants/{restaurantId}/notifications")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('RESTAURANT')")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<NotificationSummaryResponse>> getSummary(@PathVariable Long restaurantId) {
        try {
            NotificationSummaryResponse response = notificationService.getSummary(restaurantId);
            return ResponseEntity.ok(ApiResponse.success("Summary retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<WaitlistResponse>>> getNotifications(
            @PathVariable Long restaurantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String date) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<WaitlistResponse> response = notificationService.getNotifications(restaurantId, pageable, search, status, date);
            return ResponseEntity.ok(ApiResponse.success("Notifications retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{waitlistId}")
    public ResponseEntity<ApiResponse<WaitlistResponse>> getNotificationDetail(
            @PathVariable Long restaurantId,
            @PathVariable Long waitlistId) {
        try {
            WaitlistResponse response = notificationService.getNotificationDetail(restaurantId, waitlistId);
            return ResponseEntity.ok(ApiResponse.success("Notification details retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{waitlistId}/send-sms")
    public ResponseEntity<ApiResponse<SendSmsResponse>> sendSms(
            @PathVariable Long restaurantId,
            @PathVariable Long waitlistId,
            @RequestBody SendSmsRequest request) {
        try {
            SendSmsResponse response = notificationService.sendSms(restaurantId, waitlistId, request.getMessage());
            return ResponseEntity.ok(ApiResponse.success("SMS sent", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{waitlistId}/make-call")
    public ResponseEntity<ApiResponse<SendCallResponse>> makeCall(
            @PathVariable Long restaurantId,
            @PathVariable Long waitlistId,
            @RequestBody(required = false) SendSmsRequest request) {
        try {
            String message = request != null ? request.getMessage() : null;
            SendCallResponse response = notificationService.makeCall(restaurantId, waitlistId, message);
            return ResponseEntity.ok(ApiResponse.success("Phone call initiated", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{waitlistId}/sms-history")
    public ResponseEntity<ApiResponse<SmsHistoryResponse>> getSmsHistory(
            @PathVariable Long restaurantId,
            @PathVariable Long waitlistId) {
        try {
            SmsHistoryResponse response = notificationService.getSmsHistory(restaurantId, waitlistId);
            return ResponseEntity.ok(ApiResponse.success("SMS history retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}


