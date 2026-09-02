package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.Waitlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;

@Repository
public interface WaitlistRepository extends JpaRepository<Waitlist, Long>, JpaSpecificationExecutor<Waitlist> {
    List<Waitlist> findByRestaurantId(Long restaurantId);

    List<Waitlist> findByRestaurantIdAndStatus(Long restaurantId, Waitlist.WaitlistStatus status);

        Optional<Waitlist> findFirstByGuestPhoneOrderByIdDesc(String guestPhone);

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

    @Query(value = "SELECT COUNT(*) FROM waitlist w WHERE w.restaurant_id = :restaurantId AND w.sms_status = 'SENT' AND EXTRACT(YEAR FROM w.sms_sent_at) = :year AND EXTRACT(MONTH FROM w.sms_sent_at) = :month", nativeQuery = true)
    long countSentSmsThisMonth(@Param("restaurantId") Long restaurantId,
                               @Param("year") int year,
                               @Param("month") int month);

    List<Waitlist> findByStatus(Waitlist.WaitlistStatus status);

     // Aggregation queries for reports (Postgres)
     @Query(value = "SELECT COUNT(*) FROM waitlist w WHERE w.restaurant_id = :restaurantId" +
             " AND (CAST(:fromDate AS DATE) IS NULL OR DATE(w.joined_at) >= CAST(:fromDate AS DATE))" +
             " AND (CAST(:toDate AS DATE) IS NULL OR DATE(w.joined_at) <= CAST(:toDate AS DATE))", nativeQuery = true)
     long countByRestaurantInDateRange(@Param("restaurantId") Long restaurantId,
                                      @Param("fromDate") java.sql.Date fromDate,
                                      @Param("toDate") java.sql.Date toDate);

    @Query(value = "SELECT COUNT(*) FROM waitlist w WHERE w.restaurant_id = :restaurantId" +
            " AND (CAST(:fromDate AS DATE) IS NULL OR DATE(w.joined_at) >= CAST(:fromDate AS DATE))" +
            " AND (CAST(:toDate AS DATE) IS NULL OR DATE(w.joined_at) <= CAST(:toDate AS DATE))", nativeQuery = true)
    long countByRestaurantIdInDateRange(@Param("restaurantId") Long restaurantId,
                                       @Param("fromDate") java.sql.Date fromDate,
                                       @Param("toDate") java.sql.Date toDate);

     @Query(value = "SELECT COUNT(*) FROM waitlist w WHERE w.restaurant_id = :restaurantId AND w.status = :status" +
             " AND (CAST(:fromDate AS DATE) IS NULL OR DATE(w.joined_at) >= CAST(:fromDate AS DATE))" +
             " AND (CAST(:toDate AS DATE) IS NULL OR DATE(w.joined_at) <= CAST(:toDate AS DATE))", nativeQuery = true)
     long countByRestaurantAndStatusInDateRange(@Param("restaurantId") Long restaurantId,
                                                @Param("status") String status,
                                                @Param("fromDate") java.sql.Date fromDate,
                                                @Param("toDate") java.sql.Date toDate);

      @Query(value = "SELECT AVG(EXTRACT(EPOCH FROM (w.seated_at - w.joined_at))/60) FROM waitlist w " +
              "WHERE w.restaurant_id = :restaurantId AND w.seated_at IS NOT NULL" +
              " AND (CAST(:fromDate AS DATE) IS NULL OR DATE(w.joined_at) >= CAST(:fromDate AS DATE))" +
              " AND (CAST(:toDate AS DATE) IS NULL OR DATE(w.joined_at) <= CAST(:toDate AS DATE))", nativeQuery = true)
      Double averageSeatedDurationMinutes(@Param("restaurantId") Long restaurantId,
                                          @Param("fromDate") java.sql.Date fromDate,
                                          @Param("toDate") java.sql.Date toDate);

    @Query(value = "SELECT COUNT(*) FROM waitlist w " +
            "WHERE (CAST(:fromDate AS DATE) IS NULL OR DATE(w.joined_at) >= CAST(:fromDate AS DATE)) " +
            "AND (CAST(:toDate AS DATE) IS NULL OR DATE(w.joined_at) <= CAST(:toDate AS DATE))", nativeQuery = true)
    long countAllInDateRange(@Param("fromDate") java.sql.Date fromDate,
                             @Param("toDate") java.sql.Date toDate);

    @Query(value = "SELECT w.restaurant_id as restaurantId, r.name as name, COUNT(*) as joins " +
            "FROM waitlist w JOIN restaurants r ON w.restaurant_id = r.id " +
            "WHERE (CAST(:fromDate AS DATE) IS NULL OR DATE(w.joined_at) >= CAST(:fromDate AS DATE)) " +
            "AND (CAST(:toDate AS DATE) IS NULL OR DATE(w.joined_at) <= CAST(:toDate AS DATE)) " +
            "GROUP BY w.restaurant_id, r.name " +
            "ORDER BY joins DESC LIMIT :limit", nativeQuery = true)
    java.util.List<Object[]> topRestaurantsByJoins(@Param("fromDate") java.sql.Date fromDate,
                                                  @Param("toDate") java.sql.Date toDate,
                                                  @Param("limit") int limit);

    @Query(value = "SELECT w.restaurant_id as restaurantId, r.name as name, COUNT(*) as joins " +
            "FROM waitlist w JOIN restaurants r ON w.restaurant_id = r.id " +
            "WHERE w.restaurant_id = :restaurantId " +
            "AND (CAST(:fromDate AS DATE) IS NULL OR DATE(w.joined_at) >= CAST(:fromDate AS DATE)) " +
            "AND (CAST(:toDate AS DATE) IS NULL OR DATE(w.joined_at) <= CAST(:toDate AS DATE)) " +
            "GROUP BY w.restaurant_id, r.name " +
            "ORDER BY joins DESC LIMIT :limit", nativeQuery = true)
    java.util.List<Object[]> topRestaurantByJoinsForLocation(@Param("restaurantId") Long restaurantId,
                                                            @Param("fromDate") java.sql.Date fromDate,
                                                            @Param("toDate") java.sql.Date toDate,
                                                            @Param("limit") int limit);

    @Query(value = "SELECT w.guest_name as guest, w.guest_phone as contact, COUNT(*) as visits, MAX(DATE(w.joined_at)) as lastVisit " +
            "FROM waitlist w " +
            "WHERE (:restaurantId IS NULL OR w.restaurant_id = :restaurantId) " +
            "GROUP BY w.guest_name, w.guest_phone " +
            "ORDER BY visits DESC", nativeQuery = true)
    java.util.List<CustomerAggregation> aggregateCustomers(@Param("restaurantId") Long restaurantId);

       @Query("SELECT w FROM Waitlist w WHERE w.restaurant.id = :restaurantId " +
               "AND (:status IS NULL OR w.status = :status) " +
               "AND (:search IS NULL OR LOWER(w.guestName) LIKE LOWER(CONCAT('%', :search, '%')) OR w.guestPhone LIKE CONCAT('%', :search, '%')) " +
               "ORDER BY w.joinedAt DESC")
       Page<Waitlist> findByRestaurantIdWithSearch(@Param("restaurantId") Long restaurantId,
                                                    @Param("status") Waitlist.WaitlistStatus status,
                                                    @Param("search") String search,
                                                    Pageable pageable);

       @Query("SELECT w FROM Waitlist w WHERE w.restaurant.id = :restaurantId " +
               "AND (:status IS NULL OR w.status = :status) " +
               "AND (:search IS NULL OR LOWER(w.guestName) LIKE LOWER(CONCAT('%', :search, '%')) OR w.guestPhone LIKE CONCAT('%', :search, '%')) " +
               "AND DATE(w.joinedAt) = :joinedDate " +
               "ORDER BY w.joinedAt DESC")
       Page<Waitlist> findByRestaurantIdWithSearchAndDate(@Param("restaurantId") Long restaurantId,
                                                          @Param("status") Waitlist.WaitlistStatus status,
                                                          @Param("search") String search,
                                                          @Param("joinedDate") java.time.LocalDate joinedDate,
                                                          Pageable pageable);
}

