package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.TableServerAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TableServerAssignmentRepository extends JpaRepository<TableServerAssignment, Long> {

    Optional<TableServerAssignment> findByTableId(Long tableId);

    List<TableServerAssignment> findByRestaurantIdAndIsActiveTrue(Long restaurantId);

    List<TableServerAssignment> findByRestaurantIdAndAssignedServerIdAndIsActiveTrue(Long restaurantId, Long assignedServerId);

    List<TableServerAssignment> findByRestaurantId(Long restaurantId);

    void deleteByTableId(Long tableId);
}

