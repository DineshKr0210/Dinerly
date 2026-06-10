package com.restaurant.waitlist.backend.controller;

import com.restaurant.waitlist.backend.dto.request.FeedbackRequest;
import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin(origins = "*")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @PostMapping
    @PreAuthorize("hasRole('GUEST')")
    public ResponseEntity<ApiResponse<Void>> submitFeedback(@Valid @RequestBody FeedbackRequest request) {
        try {
            feedbackService.submitFeedback(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Feedback submitted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}

