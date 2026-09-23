package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.ReportRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<ReportRecord, Long> {
    Page<ReportRecord> findByGeneratedByRestaurantIdIn(List<Long> restaurantIds, Pageable pageable);
}
