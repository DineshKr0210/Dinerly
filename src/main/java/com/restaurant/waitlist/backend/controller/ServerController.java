package com.restaurant.waitlist.backend.controller;

import com.restaurant.waitlist.backend.dto.request.UpdateOrderNotesRequest;
import com.restaurant.waitlist.backend.dto.request.UpdateServerFloorRequest;
import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.dto.response.FloorRosterResponse;
import com.restaurant.waitlist.backend.dto.response.ServerAssignedTableResponse;
import com.restaurant.waitlist.backend.dto.response.ServerFloorStatusResponse;
import com.restaurant.waitlist.backend.dto.response.ServerNotificationResponse;
import com.restaurant.waitlist.backend.entity.TableServerAssignment;
import com.restaurant.waitlist.backend.repository.TableServerAssignmentRepository;
import com.restaurant.waitlist.backend.service.CurrentUserResolver;
import com.restaurant.waitlist.backend.service.ServerRotationService;
import com.restaurant.waitlist.backend.service.ServerService;
import com.restaurant.waitlist.backend.service.SettingsService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Server rotation / floor management (manager-facing) + the dedicated "My Tables" and
 * notifications feed consumed by a server's own UI screen.
 */
@RestController
@RequestMapping("/api/restaurants/{restaurantId}/servers")
@CrossOrigin(origins = "*")
@Tag(name = "Servers", description = "Server rotation, floor roster, and the server's own assigned-tables/notifications feed")
@SecurityRequirement(name = "bearerAuth")
public class ServerController {

    private static final Logger log = LoggerFactory.getLogger(ServerController.class);

    @Autowired
    private ServerRotationService serverRotationService;

    @Autowired
    private ServerService serverService;

    @Autowired
    private CurrentUserResolver currentUserResolver;

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private TableServerAssignmentRepository tableServerAssignmentRepository;

    // ------------------------------------------------------------------
    // Manager/Host-facing: floor roster & rotation management
    // ------------------------------------------------------------------

    @GetMapping("/floor")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'HOST')")
    public ResponseEntity<ApiResponse<FloorRosterResponse>> getFloorRoster(@PathVariable Long restaurantId) {
        try {
            List<ServerFloorStatusResponse> roster = serverRotationService.getFloorRoster(restaurantId);
            String mode = settingsService.getAdvancedSettings(restaurantId).getServerAssignmentMode();
            if (mode == null || mode.isBlank()) {
                mode = "ROTATION";
            }
            if ("SECTION".equalsIgnoreCase(mode)) {
                for (ServerFloorStatusResponse row : roster) {
                    List<TableServerAssignment> assignments = tableServerAssignmentRepository
                            .findByRestaurantIdAndAssignedServerIdAndIsActiveTrue(restaurantId, row.getStaffId());
                    List<String> tableNames = assignments.stream()
                            .map(a -> a.getTable() != null ? a.getTable().getTableNumber() : null)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    row.setAssignedTableNames(tableNames);
                }
            }
            FloorRosterResponse response = FloorRosterResponse.builder()
                    .serverAssignmentMode(mode)
                    .servers(roster)
                    .build();
            return ResponseEntity.ok(ApiResponse.success("Floor roster retrieved", response));
        } catch (Exception e) {
            log.error("ERROR: getFloorRoster | {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{staffId}/floor")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'HOST')")
    public ResponseEntity<ApiResponse<List<ServerFloorStatusResponse>>> setOnFloor(@PathVariable Long restaurantId,
            @PathVariable Long staffId,
            @Valid @RequestBody UpdateServerFloorRequest request) {
        try {
            boolean onFloor = request != null && Boolean.TRUE.equals(request.getOnFloor());
            Integer maxTables = request != null ? request.getMaxTables() : null;
            List<ServerFloorStatusResponse> response = serverRotationService.setOnFloor(restaurantId, staffId, onFloor, maxTables);
            return ResponseEntity.ok(ApiResponse.success(
                    onFloor ? "Server added to the floor" : "Server removed from the floor", response));
        } catch (Exception e) {
            log.error("ERROR: setOnFloor | {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/shift/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<ServerFloorStatusResponse>>> startNewShift(@PathVariable Long restaurantId) {
        try {
            List<ServerFloorStatusResponse> response = serverRotationService.startNewShift(restaurantId);
            return ResponseEntity.ok(ApiResponse.success("New shift started - seated counts reset", response));
        } catch (Exception e) {
            log.error("ERROR: startNewShift | {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ------------------------------------------------------------------
    // Server-facing: "my tables" + notifications (separate server UI screen)
    // ------------------------------------------------------------------

    @GetMapping("/me/tables")
    @PreAuthorize("hasAnyRole('SERVER', 'ADMIN', 'MANAGER', 'HOST')")
    public ResponseEntity<ApiResponse<List<ServerAssignedTableResponse>>> getMyTables(@PathVariable Long restaurantId) {
        try {
            Long staffId = currentUserResolver.getCurrentStaffId();
            if (staffId == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("No staff record linked to the current user"));
            }
            serverService.resolveStaffId(restaurantId, staffId);
            List<ServerAssignedTableResponse> response = serverService.getMyTables(restaurantId, staffId);
            return ResponseEntity.ok(ApiResponse.success("Assigned tables retrieved", response));
        } catch (Exception e) {
            log.error("ERROR: getMyTables | {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/me/tables/{waitlistId}/notes")
    @PreAuthorize("hasRole('SERVER')")
    public ResponseEntity<ApiResponse<ServerAssignedTableResponse>> updateOrderNotes(@PathVariable Long restaurantId,
            @PathVariable Long waitlistId,
            @Valid @RequestBody UpdateOrderNotesRequest request) {
        try {
            Long staffId = currentUserResolver.getCurrentStaffId();
            if (staffId == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("No staff record linked to the current user"));
            }
            serverService.resolveStaffId(restaurantId, staffId);
            ServerAssignedTableResponse response = serverService.updateOrderNotes(
                    restaurantId, staffId, waitlistId, request != null ? request.getOrderNotes() : null);
            return ResponseEntity.ok(ApiResponse.success("Order notes saved", response));
        } catch (Exception e) {
            log.error("ERROR: updateOrderNotes | {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/me/notifications")
    @PreAuthorize("hasAnyRole('SERVER', 'ADMIN', 'MANAGER', 'HOST')")
    public ResponseEntity<ApiResponse<Page<ServerNotificationResponse>>> getMyNotifications(@PathVariable Long restaurantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Long staffId = currentUserResolver.getCurrentStaffId();
            if (staffId == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("No staff record linked to the current user"));
            }
            serverService.resolveStaffId(restaurantId, staffId);
            Pageable pageable = PageRequest.of(Math.max(page, 0), size > 0 ? size : 20);
            Page<ServerNotificationResponse> response = serverService.getMyNotifications(restaurantId, staffId, pageable);
            return ResponseEntity.ok(ApiResponse.success("Notifications retrieved", response));
        } catch (Exception e) {
            log.error("ERROR: getMyNotifications | {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/me/notifications/unread-count")
    @PreAuthorize("hasAnyRole('SERVER', 'ADMIN', 'MANAGER', 'HOST')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadNotificationCount(@PathVariable Long restaurantId) {
        try {
            Long staffId = currentUserResolver.getCurrentStaffId();
            if (staffId == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("No staff record linked to the current user"));
            }
            serverService.resolveStaffId(restaurantId, staffId);
            long count = serverService.getUnreadNotificationCount(restaurantId, staffId);
            return ResponseEntity.ok(ApiResponse.success("Unread count retrieved", Map.of("unreadCount", count)));
        } catch (Exception e) {
            log.error("ERROR: getUnreadNotificationCount | {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/me/notifications/{notificationId}/read")
    @PreAuthorize("hasAnyRole('SERVER', 'ADMIN', 'MANAGER', 'HOST')")
    public ResponseEntity<ApiResponse<Void>> markNotificationRead(@PathVariable Long restaurantId,
            @PathVariable Long notificationId) {
        try {
            Long staffId = currentUserResolver.getCurrentStaffId();
            if (staffId == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("No staff record linked to the current user"));
            }
            serverService.resolveStaffId(restaurantId, staffId);
            serverService.markNotificationRead(restaurantId, staffId, notificationId);
            return ResponseEntity.ok(ApiResponse.success("Notification marked as read"));
        } catch (Exception e) {
            log.error("ERROR: markNotificationRead | {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

