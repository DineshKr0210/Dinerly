package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.RewardSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RewardSettingsRepository extends JpaRepository<RewardSettings, Long> {
    Optional<RewardSettings> findTopByOrderByIdDesc();
}
