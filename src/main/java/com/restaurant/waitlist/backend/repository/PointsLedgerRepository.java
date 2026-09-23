package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.PointsLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PointsLedgerRepository extends JpaRepository<PointsLedger, Long> {
}
