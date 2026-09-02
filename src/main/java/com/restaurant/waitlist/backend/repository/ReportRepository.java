package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.ReportRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<ReportRecord, Long> {
}
