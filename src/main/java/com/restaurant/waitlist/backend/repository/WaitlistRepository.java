package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.Waitlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;

@Repository
public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {
    List<Waitlist> findByRestaurantId(Long restaurantId);

    List<Waitlist> findByRestaurantIdAndStatus(Long restaurantId, Waitlist.WaitlistStatus status);

    Optional<Waitlist> findByGuestPhone(String guestPhone);

    // Return the most recent waitlist entry (by joined_at desc then id desc) for a given restaurant and guest phone
    @Query(value = "SELECT * FROM waitlist w WHERE w.restaurant_id = :restaurantId AND w.guest_phone = :guestPhone ORDER BY w.id DESC LIMIT 1", nativeQuery = true)
    Optional<Waitlist> findLatestByRestaurantIdAndGuestPhone(@Param("restaurantId") Long restaurantId, @Param("guestPhone") String guestPhone);

    long countByRestaurantIdAndStatusIn(Long restaurantId, java.util.Collection<Waitlist.WaitlistStatus> statuses);

    java.util.List<Waitlist> findByRestaurantIdAndStatusInOrderByIdAsc(Long restaurantId, java.util.Collection<Waitlist.WaitlistStatus> statuses);

    @Query(value = "SELECT * FROM waitlist w WHERE w.restaurant_id = :restaurantId AND DATE(w.joined_at) = :joinedDate", nativeQuery = true)
    java.util.List<Waitlist> findByRestaurantIdAndJoinedDate(@Param("restaurantId") Long restaurantId, @Param("joinedDate") java.sql.Date joinedDate);

    @Query("SELECT w FROM Waitlist w WHERE w.restaurant.id = :restaurantId AND w.guestPhone = :guestPhone ORDER BY w.id DESC")
    List<Waitlist> findAllByRestaurantIdAndGuestPhone(@Param("restaurantId") Long restaurantId, @Param("guestPhone") String guestPhone);

    @Query("SELECT w FROM Waitlist w WHERE w.restaurant.id = ?1 AND w.status IN ('WAITING', 'NOTIFIED') ORDER BY w.position ASC")
    List<Waitlist> findActiveWaitlistByRestaurant(Long restaurantId);

    @Query("SELECT COUNT(w) FROM Waitlist w WHERE w.restaurant.id = ?1 AND w.status = 'SEATED'")
    long countSeatedByRestaurant(Long restaurantId);

    List<Waitlist> findByStatus(Waitlist.WaitlistStatus status);
}

