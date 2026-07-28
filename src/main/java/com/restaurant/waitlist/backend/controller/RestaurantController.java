package com.restaurant.waitlist.backend.controller;

import com.restaurant.waitlist.backend.dto.request.AddGuestRequest;
import com.restaurant.waitlist.backend.dto.request.AddTableRequest;
import com.restaurant.waitlist.backend.dto.request.CreateRestaurantRequest;
import com.restaurant.waitlist.backend.dto.request.NotifyGuestRequest;
import com.restaurant.waitlist.backend.dto.request.SeatGuestRequest;
import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.dto.response.DashboardStatsResponse;
import com.restaurant.waitlist.backend.dto.response.RestaurantResponse;
import com.restaurant.waitlist.backend.dto.response.TableResponse;
import com.restaurant.waitlist.backend.dto.response.WaitlistResponse;
import com.restaurant.waitlist.backend.dto.response.WaitlistSmsResult;
import com.restaurant.waitlist.backend.dto.response.ReportsResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Page;
import com.restaurant.waitlist.backend.entity.Table;
import com.restaurant.waitlist.backend.service.RestaurantService;
import com.restaurant.waitlist.backend.service.TableService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@CrossOrigin(origins = "*")
@SecurityRequirement(name = "bearerAuth")
public class RestaurantController {

    private static final Logger log = LoggerFactory.getLogger(RestaurantController.class);

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private TableService tableService;

    @PostMapping
    public ResponseEntity<ApiResponse<RestaurantResponse>> createRestaurant(
            @Valid @RequestBody CreateRestaurantRequest request) {
        try {
            log.info("START: createRestaurant | {}", request);
            RestaurantResponse response = restaurantService.createRestaurant(request);
            log.info("END: createRestaurant | success");
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Restaurant created successfully", response));
        } catch (Exception e) {
            log.error("ERROR: createRestaurant | {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{restaurantId}/waitlist")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<List<WaitlistResponse>>> getWaitlist(@PathVariable Long restaurantId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String date) {
        try {
            log.info("START: getWaitlist | restaurantId={}, status={}, date={}", restaurantId, status, date);
            List<WaitlistResponse> response = restaurantService.getWaitlist(restaurantId, status, date);
            log.info("END: getWaitlist | success");
            return ResponseEntity.ok(ApiResponse.success("Waitlist retrieved", response));
        } catch (Exception e) {
            log.error("ERROR: getWaitlist | {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{restaurantId}/waitlist/{id}/status")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<WaitlistResponse>> getWaitlistStatusById(@PathVariable Long restaurantId,
            @PathVariable Long id) {
        try {
            log.info("START: getWaitlistStatusById | restaurantId={}, id={}", restaurantId, id);
            WaitlistResponse response = restaurantService.getWaitlistStatusById(restaurantId, id);
            log.info("END: getWaitlistStatusById | success");
            return ResponseEntity.ok(ApiResponse.success("Waitlist status retrieved", response));
        } catch (Exception e) {
            log.error("ERROR: getWaitlistStatusById | {}", e.getMessage());
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
            log.info("START: addGuest | restaurantId={}, request={}", restaurantId, request);
            WaitlistResponse response = restaurantService.addGuestToWaitlist(restaurantId, request);
            log.info("END: addGuest | success");
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Guest added to waitlist", response));
        } catch (Exception e) {
            log.error("ERROR: addGuest | {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{restaurantId}/waitlist/{id}/notify")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<WaitlistSmsResult>> notifyGuest(
            @PathVariable Long restaurantId,
            @PathVariable Long id,
            @Valid @RequestBody NotifyGuestRequest request) {
        try {
            log.info("START: notifyGuest | restaurantId={}, id={}, request={}", restaurantId, id, request);
            WaitlistSmsResult result = restaurantService.notifyGuest(restaurantId, id, request);
            log.info("END: notifyGuest | success smsSent={}", result.isSmsSent());
            return ResponseEntity.ok(ApiResponse.success("Guest notified successfully", result));
        } catch (Exception e) {
            log.error("ERROR: notifyGuest | {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{restaurantId}/waitlist/{id}/approve")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<WaitlistSmsResult>> approveGuest(@PathVariable Long restaurantId, @PathVariable Long id,
            @Valid @RequestBody NotifyGuestRequest request) {
        try {
            log.info("START: approveGuest | restaurantId={}, id={}, request={}", restaurantId, id, request);
            WaitlistSmsResult result = restaurantService.approveGuest(restaurantId, id, request);
            log.info("END: approveGuest | success smsSent={}", result.isSmsSent());
            return ResponseEntity.ok(ApiResponse.success("Guest approved successfully", result));
        } catch (Exception e) {
            log.error("ERROR: approveGuest | {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{restaurantId}/waitlist/{id}/update-estimate")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<Void>> updateEstimate(@PathVariable Long restaurantId, @PathVariable Long id,
            @Valid @RequestBody NotifyGuestRequest request) {
        try {
            log.info("START: updateEstimate | restaurantId={}, id={}, request={}", restaurantId, id, request);
            restaurantService.updateEstimate(restaurantId, id, request);
            log.info("END: updateEstimate | success");
            return ResponseEntity.ok(ApiResponse.success("Estimate updated"));
        } catch (Exception e) {
            log.error("ERROR: updateEstimate | {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{restaurantId}/waitlist/{id}/seat")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<WaitlistResponse>> seatGuest(@PathVariable Long restaurantId, @PathVariable Long id,
            @Valid @RequestBody(required = false) SeatGuestRequest request) {
        try {
            log.info("START: seatGuest | restaurantId={}, id={}, request={}", restaurantId, id, request);
            WaitlistResponse response = restaurantService.seatGuest(restaurantId, id, request);
            log.info("END: seatGuest | success");
            return ResponseEntity.ok(ApiResponse.success("Guest seated successfully", response));
        } catch (Exception e) {
            log.error("ERROR: seatGuest | {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{restaurantId}/waitlist/rejoin/{id}")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<WaitlistResponse>> rejoinGuest(@PathVariable Long restaurantId,
            @PathVariable Long id) {
        try {
            log.info("START: rejoinGuest | restaurantId={}, id={}", restaurantId, id);
            WaitlistResponse response = restaurantService.rejoinGuest(restaurantId, id);
            log.info("END: rejoinGuest | success");
            return ResponseEntity.ok(ApiResponse.success("Guest rejoined successfully", response));
        } catch (Exception e) {
            log.error("ERROR: rejoinGuest | {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{restaurantId}/waitlist/{id}")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<WaitlistResponse>> removeGuest(@PathVariable Long restaurantId, @PathVariable Long id) {
        try {
            log.info("START: removeGuest | restaurantId={}, id={}", restaurantId, id);
            WaitlistResponse response = restaurantService.removeGuest(restaurantId, id);
            log.info("END: removeGuest | success");
            return ResponseEntity.ok(ApiResponse.success("Guest removed", response));
        } catch (Exception e) {
            log.error("ERROR: removeGuest | {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{restaurantId}/tables")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<List<TableResponse>>> getTables(@PathVariable Long restaurantId) {
        try {
            log.info("START: getTables | restaurantId={}", restaurantId);
            List<TableResponse> response = tableService.getRestaurantTables(restaurantId);
            log.info("END: getTables | success");
            return ResponseEntity.ok(ApiResponse.success("Tables retrieved", response));
        } catch (Exception e) {
            log.error("ERROR: getTables | {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{restaurantId}/tables")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<TableResponse>> addTable(@PathVariable Long restaurantId,
            @Valid @RequestBody AddTableRequest request) {
        try {
            log.info("START: addTable | restaurantId={}, request={}", restaurantId, request);
            TableResponse response = tableService.addTable(restaurantId, request);
            log.info("END: addTable | success");
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Table added successfully", response));
        } catch (Exception e) {
            log.error("ERROR: addTable | {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{restaurantId}/tables/{tableId}/status")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<Void>> updateTableStatus(@PathVariable Long restaurantId, @PathVariable Long tableId,
            @RequestParam Table.TableStatus status) {
        try {
            log.info("START: updateTableStatus | restaurantId={}, tableId={}, status={}", restaurantId, tableId, status);
            tableService.updateTableStatus(restaurantId, tableId, status);
            log.info("END: updateTableStatus | success");
            return ResponseEntity.ok(ApiResponse.success("Table status updated successfully"));
        } catch (Exception e) {
            log.error("ERROR: updateTableStatus | {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{restaurantId}/dashboard")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboard(@PathVariable Long restaurantId) {
        try {
            log.info("START: getDashboard | restaurantId={}", restaurantId);
            DashboardStatsResponse response = restaurantService.getDashboardStats(restaurantId);
            log.info("END: getDashboard | success");
            return ResponseEntity.ok(ApiResponse.success("Dashboard stats retrieved", response));
        } catch (Exception e) {
            log.error("ERROR: getDashboard | {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    @GetMapping("/{restaurantId}/guest-history")
    @PreAuthorize("hasRole('RESTAURANT')")
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

            Page<WaitlistResponse> response = restaurantService.getGuestHistory(restaurantId, page, size, status, date);
            log.info("END: getGuestHistory | success");
            return ResponseEntity.ok(ApiResponse.success("Guest history retrieved", response));
        } catch (Exception e) {
            log.error("ERROR: getGuestHistory | {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{restaurantId}/guest-history/export")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<?> exportGuestHistory(@PathVariable Long restaurantId,
                                                @RequestParam(required = false) String status,
                                                @RequestParam(required = false) String date) {
        try {
            log.info("START: exportGuestHistory | restaurantId={}, status={}, date={}", restaurantId, status, date);
            String csv = restaurantService.exportGuestHistoryCsv(restaurantId, status, date);
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

}

