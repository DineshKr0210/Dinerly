package com.restaurant.waitlist.backend.controller;

import com.restaurant.waitlist.backend.dto.request.AddGuestRequest;
import com.restaurant.waitlist.backend.dto.request.AddTableRequest;
import com.restaurant.waitlist.backend.dto.request.CreateRestaurantRequest;
import com.restaurant.waitlist.backend.dto.request.NotifyGuestRequest;
import com.restaurant.waitlist.backend.dto.request.UpdateRestaurantSettingsRequest;
import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.dto.response.DashboardStatsResponse;
import com.restaurant.waitlist.backend.dto.response.RestaurantResponse;
import com.restaurant.waitlist.backend.dto.response.RestaurantSettingsResponse;
import com.restaurant.waitlist.backend.dto.response.TableResponse;
import com.restaurant.waitlist.backend.dto.response.WaitlistResponse;
import com.restaurant.waitlist.backend.entity.Table;
import com.restaurant.waitlist.backend.service.RestaurantService;
import com.restaurant.waitlist.backend.service.TableService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@CrossOrigin(origins = "*")
public class RestaurantController {

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private TableService tableService;

    @PostMapping
    public ResponseEntity<ApiResponse<RestaurantResponse>> createRestaurant(
            @Valid @RequestBody CreateRestaurantRequest request) {
        try {
            RestaurantResponse response = restaurantService.createRestaurant(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Restaurant created successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> getAllRestaurants() {
        try {
            List<RestaurantResponse> response = restaurantService.getAllRestaurants();
            return ResponseEntity.ok(ApiResponse.success("Restaurants retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{restaurantId}/waitlist")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<List<WaitlistResponse>>> getWaitlist(@PathVariable Long restaurantId) {
        try {
            List<WaitlistResponse> response = restaurantService.getWaitlist(restaurantId);
            return ResponseEntity.ok(ApiResponse.success("Waitlist retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{restaurantId}/waitlist")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<WaitlistResponse>> addGuest(
            @PathVariable Long restaurantId,
            @Valid @RequestBody AddGuestRequest request) {
        try {
            WaitlistResponse response = restaurantService.addGuestToWaitlist(restaurantId, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Guest added to waitlist", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{restaurantId}/waitlist/{id}/notify")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<Void>> notifyGuest(
            @PathVariable Long restaurantId,
            @PathVariable Long id,
            @Valid @RequestBody NotifyGuestRequest request) {
        try {
            restaurantService.notifyGuest(restaurantId, id, request);
            return ResponseEntity.ok(ApiResponse.success("Guest notified successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{restaurantId}/waitlist/{id}/seat")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<Void>> seatGuest(@PathVariable Long restaurantId, @PathVariable Long id) {
        try {
            restaurantService.seatGuest(restaurantId, id);
            return ResponseEntity.ok(ApiResponse.success("Guest seated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{restaurantId}/waitlist/{id}")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<Void>> removeGuest(@PathVariable Long restaurantId, @PathVariable Long id) {
        try {
            restaurantService.removeGuest(restaurantId, id);
            return ResponseEntity.ok(ApiResponse.success("Guest removed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{restaurantId}/tables")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<List<TableResponse>>> getTables(@PathVariable Long restaurantId) {
        try {
            List<TableResponse> response = tableService.getRestaurantTables(restaurantId);
            return ResponseEntity.ok(ApiResponse.success("Tables retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{restaurantId}/tables")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<TableResponse>> addTable(@PathVariable Long restaurantId,
            @Valid @RequestBody AddTableRequest request) {
        try {
            TableResponse response = tableService.addTable(restaurantId, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Table added successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{restaurantId}/tables/{tableId}/status")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<Void>> updateTableStatus(@PathVariable Long restaurantId, @PathVariable Long tableId,
            @RequestParam Table.TableStatus status) {
        try {
            tableService.updateTableStatus(restaurantId, tableId, status);
            return ResponseEntity.ok(ApiResponse.success("Table status updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{restaurantId}/dashboard")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboard(@PathVariable Long restaurantId) {
        try {
            DashboardStatsResponse response = restaurantService.getDashboardStats(restaurantId);
            return ResponseEntity.ok(ApiResponse.success("Dashboard stats retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{restaurantId}/guest-history")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<List<WaitlistResponse>>> getGuestHistory(@PathVariable Long restaurantId) {
        try {
            List<WaitlistResponse> response = restaurantService.getGuestHistory(restaurantId);
            return ResponseEntity.ok(ApiResponse.success("Guest history retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{restaurantId}/settings")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<RestaurantSettingsResponse>> getSettings(@PathVariable Long restaurantId) {
        try {
            RestaurantSettingsResponse response = restaurantService.getSettings(restaurantId);
            return ResponseEntity.ok(ApiResponse.success("Settings retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{restaurantId}/settings")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<RestaurantSettingsResponse>> updateSettings(@PathVariable Long restaurantId,
            @Valid @RequestBody UpdateRestaurantSettingsRequest request) {
        try {
            RestaurantSettingsResponse response = restaurantService.updateSettings(restaurantId, request);
            return ResponseEntity.ok(ApiResponse.success("Settings updated successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}

