package com.restaurant.waitlist.backend.controller;

import com.restaurant.waitlist.backend.dto.request.AssignServerToTableRequest;
import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.dto.response.TableServerAssignmentResponse;
import com.restaurant.waitlist.backend.entity.TableServerAssignment;
import com.restaurant.waitlist.backend.service.ServerSectionAssignmentService;
import com.restaurant.waitlist.backend.service.StaffService;
import com.restaurant.waitlist.backend.service.TableService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Endpoint for managing table-server section assignments (Scenario 2).
 * These endpoints are typically used by managers to set up which server owns which tables.
 */
@RestController
@RequestMapping("/api/restaurants/{restaurantId}/tables")
@CrossOrigin(origins = "*")
@Tag(name = "Table Server Assignment", description = "Section-based table-to-server assignments (Scenario 2)")
public class TableServerAssignmentController {

    private static final Logger log = LoggerFactory.getLogger(TableServerAssignmentController.class);

    @Autowired
    private ServerSectionAssignmentService serverSectionAssignmentService;

    @Autowired
    private StaffService staffService;

    @Autowired
    private TableService tableService;

    /**
     * Assign a server to a table (for section-based seating).
     * POST /api/restaurants/{restaurantId}/tables/{tableId}/assign-server
     */
    @PostMapping("/{tableId}/assign-server")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'HOST')")
    public ResponseEntity<ApiResponse<TableServerAssignmentResponse>> assignServerToTable(
            @PathVariable Long restaurantId,
            @PathVariable Long tableId,
            @Valid @RequestBody AssignServerToTableRequest request) {
        try {
            log.info("START: assignServerToTable | restaurantId={}, tableId={}, serverId={}",
                    restaurantId, tableId, request.getServerId());

            TableServerAssignment assignment = serverSectionAssignmentService.assignServerToTable(
                    restaurantId,
                    tableId,
                    request.getServerId(),
                    request.getSectionName()
            );

            String tableName = tableService.getTable(restaurantId, tableId).getTableNumber();
            String serverName = staffService.getStaffById(request.getServerId()).getName();

            TableServerAssignmentResponse response = TableServerAssignmentResponse.fromEntity(
                    assignment,
                    tableName,
                    serverName
            );

            log.info("END: assignServerToTable | success");
            return ResponseEntity.ok(ApiResponse.success("Server assigned to table successfully", response));
        } catch (Exception e) {
            log.error("ERROR: assignServerToTable | {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get all table-server assignments for a restaurant.
     * GET /api/restaurants/{restaurantId}/tables/server-assignments
     */
    @GetMapping("/server-assignments")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'HOST')")
    public ResponseEntity<ApiResponse<List<TableServerAssignmentResponse>>> getAllAssignments(
            @PathVariable Long restaurantId) {
        try {
            log.info("START: getAllAssignments | restaurantId={}", restaurantId);

            List<TableServerAssignment> assignments = serverSectionAssignmentService.getAssignmentsForRestaurant(restaurantId);
            
            List<TableServerAssignmentResponse> responses = assignments.stream()
                    .map(assignment -> {
                        String tableName = assignment.getTable() != null ? assignment.getTable().getTableNumber() : "Unknown";
                        String serverName = "Unknown";
                        try {
                            serverName = staffService.getStaffById(assignment.getAssignedServerId()).getName();
                        } catch (Exception e) {
                            log.warn("Could not fetch server name for staff id={}", assignment.getAssignedServerId());
                        }
                        return TableServerAssignmentResponse.fromEntity(assignment, tableName, serverName);
                    })
                    .collect(Collectors.toList());

            log.info("END: getAllAssignments | found {} assignments", responses.size());
            return ResponseEntity.ok(ApiResponse.success("Table-server assignments retrieved successfully", responses));
        } catch (Exception e) {
            log.error("ERROR: getAllAssignments | {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get all tables assigned to a specific server.
     * GET /api/restaurants/{restaurantId}/servers/{serverId}/assigned-tables
     */
    @GetMapping("/assigned-tables/{serverId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'HOST', 'SERVER')")
    public ResponseEntity<ApiResponse<List<TableServerAssignmentResponse>>> getTablesForServer(
            @PathVariable Long restaurantId,
            @PathVariable Long serverId) {
        try {
            log.info("START: getTablesForServer | restaurantId={}, serverId={}", restaurantId, serverId);

            List<TableServerAssignment> assignments = serverSectionAssignmentService.getTablesForServer(restaurantId, serverId);
            
            List<TableServerAssignmentResponse> responses = assignments.stream()
                    .map(assignment -> {
                        String tableName = assignment.getTable() != null ? assignment.getTable().getTableNumber() : "Unknown";
                        String serverName = staffService.getStaffById(serverId).getName();
                        return TableServerAssignmentResponse.fromEntity(assignment, tableName, serverName);
                    })
                    .collect(Collectors.toList());

            log.info("END: getTablesForServer | found {} tables for server", responses.size());
            return ResponseEntity.ok(ApiResponse.success("Tables for server retrieved successfully", responses));
        } catch (Exception e) {
            log.error("ERROR: getTablesForServer | {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Deactivate a table-server assignment.
     * DELETE /api/restaurants/{restaurantId}/tables/server-assignments/{assignmentId}
     */
    @DeleteMapping("/server-assignments/{assignmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'HOST')")
    public ResponseEntity<ApiResponse<String>> deactivateAssignment(
            @PathVariable Long restaurantId,
            @PathVariable Long assignmentId) {
        try {
            log.info("START: deactivateAssignment | restaurantId={}, assignmentId={}", restaurantId, assignmentId);

            serverSectionAssignmentService.deactivateAssignment(assignmentId);

            log.info("END: deactivateAssignment | success");
            return ResponseEntity.ok(ApiResponse.success("Table-server assignment deactivated successfully", null));
        } catch (Exception e) {
            log.error("ERROR: deactivateAssignment | {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}

