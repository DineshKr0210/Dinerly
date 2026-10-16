package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.entity.TableServerAssignment;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.repository.StaffRepository;
import com.restaurant.waitlist.backend.repository.TableRepository;
import com.restaurant.waitlist.backend.repository.TableServerAssignmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implements the "Section-Based Server Assignment" (Scenario 2).
 *
 * How it works:
 * - Each table is pre-assigned to a specific server (section owner)
 * - When a guest sits at a table, that table's assigned server gets the booking
 * - No rotation pointer or queue logic; just table -> server mapping
 * - Fairness depends on table distribution and demand patterns
 */
@Service
public class ServerSectionAssignmentService {

    private static final Logger log = LoggerFactory.getLogger(ServerSectionAssignmentService.class);

    @Autowired
    private TableServerAssignmentRepository tableServerAssignmentRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private TableRepository tableRepository;

    // =====================================================================
    // Table-Server Assignment Management
    // =====================================================================

    /**
     * Assign aserver to a table. If assignment already exists, update it.
     */
    @Transactional
    public TableServerAssignment assignServerToTable(Long restaurantId, Long tableId, Long serverId, String sectionName) {
        restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table not found"));

        staffRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Staff member not found"));

        Optional<TableServerAssignment> existing = tableServerAssignmentRepository.findByTableId(tableId);

        TableServerAssignment assignment;
        if (existing.isPresent()) {
            assignment = existing.get();
            assignment.setAssignedServerId(serverId);
            assignment.setSectionName(sectionName);
            assignment.setIsActive(true);
            log.info("Updated table-server assignment: tableId={}, serverId={}, sectionName={}",
                    tableId, serverId, sectionName);
        } else {
            assignment = TableServerAssignment.builder()
                    .restaurant(restaurantRepository.findById(restaurantId).get())
                    .table(tableRepository.findById(tableId).get())
                    .assignedServerId(serverId)
                    .sectionName(sectionName)
                    .isActive(true)
                    .build();
            log.info("Created new table-server assignment: tableId={}, serverId={}, sectionName={}",
                    tableId, serverId, sectionName);
        }

        return tableServerAssignmentRepository.save(assignment);
    }

    /**
     * Get the assigned server for a table. Returns the staffId, or null if not assigned.
     * Used during seating to determine which server should receive the booking.
     */
    public Long getServerForTable(Long tableId) {
        Optional<TableServerAssignment> assignment = tableServerAssignmentRepository.findByTableId(tableId);

        if (assignment.isPresent() && Boolean.TRUE.equals(assignment.get().getIsActive())) {
            return assignment.get().getAssignedServerId();
        }

        log.warn("No active server assignment found for tableId={}", tableId);
        return null;
    }

    /**
     * Get all table-server assignments for a restaurant (active only)
     */
    public List<TableServerAssignment> getAssignmentsForRestaurant(Long restaurantId) {
        return tableServerAssignmentRepository.findByRestaurantIdAndIsActiveTrue(restaurantId);
    }

    /**
     * Get all tables assigned to a specific server in a restaurant
     */
    public List<TableServerAssignment> getTablesForServer(Long restaurantId, Long serverId) {
        return tableServerAssignmentRepository.findByRestaurantIdAndAssignedServerIdAndIsActiveTrue(restaurantId, serverId);
    }

    /**
     * Deactivate a table-server assignment (soft delete)
     */
    @Transactional
    public void deactivateAssignment(Long assignmentId) {
        tableServerAssignmentRepository.findById(assignmentId)
                .ifPresentOrElse(
                        assignment -> {
                            assignment.setIsActive(false);
                            tableServerAssignmentRepository.save(assignment);
                            log.info("Deactivated table-server assignment: id={}", assignmentId);
                        },
                        () -> {
                            throw new RuntimeException("Table-server assignment not found");
                        }
                );
    }

    /**
     * Resolve the server for a seating event using table-based assignment.
     * This is the main entry point during the seating process.
     *
     * @param restaurantId the restaurant
     * @param tableId the table where the guest will sit (required for section-based assignment)
     * @return the assigned server's staffId, or null if table is not assigned to any server
     */
    @Transactional
    public Long assignServerForSeating(Long restaurantId, Long tableId) {
        if (tableId == null) {
            log.warn("assignServerForSeating | tableId is required for section-based assignment");
            return null;
        }

        Long serverId = getServerForTable(tableId);
        if (serverId != null) {
            log.info("Assigned server {} to table {} (section-based assignment)", serverId, tableId);
        } else {
            log.warn("No server assigned to tableId={}", tableId);
        }

        return serverId;
    }
}


