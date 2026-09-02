package com.restaurant.waitlist.backend.controller.admin;

import com.restaurant.waitlist.backend.dto.request.admin.LocationRequest;
import com.restaurant.waitlist.backend.dto.response.admin.LocationResponse;
import com.restaurant.waitlist.backend.dto.response.admin.LocationsPageResponse;
import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.service.admin.AdminLocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/locations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminLocationController {

    private final AdminLocationService adminLocationService;

    @GetMapping
    public ResponseEntity<ApiResponse<Object>> list(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        LocationsPageResponse resp = adminLocationService.listLocations(pageable);
        return ResponseEntity.ok(ApiResponse.success("Data retrieved successfully", resp));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LocationResponse>> create(@Valid @RequestBody LocationRequest request) {
        LocationResponse resp = adminLocationService.createLocation(request);
        return ResponseEntity.ok(ApiResponse.success("Location created successfully", resp));
    }

    @GetMapping("/{locationId}")
    public ResponseEntity<ApiResponse<LocationResponse>> get(@PathVariable Long locationId) {
        LocationResponse resp = adminLocationService.getLocation(locationId);
        if (resp == null) return ResponseEntity.status(404).body(ApiResponse.error("Location not found"));
        return ResponseEntity.ok(ApiResponse.success("Data retrieved successfully", resp));
    }

    @PutMapping("/{locationId}")
    public ResponseEntity<ApiResponse<LocationResponse>> update(@PathVariable Long locationId, @Valid @RequestBody LocationRequest request) {
        LocationResponse resp = adminLocationService.updateLocation(locationId, request);
        return ResponseEntity.ok(ApiResponse.success("Location updated successfully", resp));
    }
}
