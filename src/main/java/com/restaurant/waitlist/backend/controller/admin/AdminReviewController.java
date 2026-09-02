package com.restaurant.waitlist.backend.controller.admin;

import com.restaurant.waitlist.backend.dto.request.admin.ReviewReplyRequest;
import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.dto.response.admin.ReviewResponse;
import com.restaurant.waitlist.backend.service.admin.AdminReviewService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reviews")
@CrossOrigin(origins = "*")
@SecurityRequirement(name = "bearerAuth")
public class AdminReviewController {

    @Autowired
    private AdminReviewService adminReviewService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> listReviews(
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) String filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewResponse> result = adminReviewService.listReviews(locationId, filter, pageable);
        return ResponseEntity.ok(ApiResponse.success("Data retrieved successfully", result));
    }

    @PostMapping("/{reviewId}/reply")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> replyToReview(@PathVariable Long reviewId, @Valid @RequestBody ReviewReplyRequest request) {
        try {
            adminReviewService.replyToReview(reviewId, request.getReply());
            return ResponseEntity.ok(ApiResponse.success("Review reply posted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
