package com.restaurant.waitlist.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Maps each table to an assigned server (Scenario 2: Section-based assignment).
 * When serverAssignmentMode = "SECTION", this table is used to determine which server
 * should be credited with seating when a guest sits at that table.
 */
@Entity
@jakarta.persistence.Table(name = "table_server_assignment", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"table_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableServerAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne
    @JoinColumn(name = "table_id", nullable = false)
    private Table table;

    // Staff.id of the server assigned to this table
    @Column(name = "assigned_server_id", nullable = false)
    private Long assignedServerId;

    // Optional: descriptive name for the section (e.g., "Window", "Bar", "Patio")
    @Column(name = "section_name")
    private String sectionName;

    // To deactivate an assignment without deleting (soft delete pattern)
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

