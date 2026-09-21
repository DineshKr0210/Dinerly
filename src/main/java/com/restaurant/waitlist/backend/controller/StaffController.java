package com.restaurant.waitlist.backend.controller;

import com.restaurant.waitlist.backend.dto.request.CreateStaffRequest;
import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.dto.response.StaffResponse;
import com.restaurant.waitlist.backend.service.StaffService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants/{restaurantId}/staff")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class StaffController {

    @Autowired
    private StaffService staffService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<StaffResponse>>> getStaff(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(ApiResponse.success("Staff retrieved", staffService.getStaff(restaurantId)));
    }
}
