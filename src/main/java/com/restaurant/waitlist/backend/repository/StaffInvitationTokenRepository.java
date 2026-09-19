package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.StaffInvitationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffInvitationTokenRepository extends JpaRepository<StaffInvitationToken, Long> {
    Optional<StaffInvitationToken> findByToken(String token);

    Optional<StaffInvitationToken> findByStaffIdAndIsUsedFalse(Long staffId);
}

