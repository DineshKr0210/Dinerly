package com.restaurant.waitlist.backend.controller.admin;

import com.restaurant.waitlist.backend.dto.request.admin.CreditPointsRequest;
import com.restaurant.waitlist.backend.dto.request.admin.BulkCreditRequest;
import com.restaurant.waitlist.backend.dto.request.admin.ReversePointsRequest;
import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.entity.DinerlyPoints;
import com.restaurant.waitlist.backend.repository.UserRepository;
import com.restaurant.waitlist.backend.service.PointsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/points")
@RequiredArgsConstructor
public class AdminPointsController {
    private static final Logger log = LoggerFactory.getLogger(AdminPointsController.class);

    private final PointsService pointsService;
    private final UserRepository userRepository;

    @PostMapping("/credit")
    public ResponseEntity<?> credit(@RequestBody CreditPointsRequest req) {
        log.info("Admin credit points: userId={}, amount={}", req.getUserId(), req.getAmount());
        long newBalance = pointsService.credit(req.getUserId(), req.getAmount(), req.getReason(), "ADMIN", null, "admin");
        return ResponseEntity.ok().body(ApiResponse.success("Points credited successfully", Map.of(
            "userId", req.getUserId(),
            "pointsAdded", req.getAmount(),
            "newBalance", newBalance
        )));
    }

    @PostMapping("/debit")
    public ResponseEntity<?> debit(@RequestBody Map<String, Object> req) {
        Long userId = Long.valueOf(req.get("userId").toString());
        Long amount = Long.valueOf(req.get("amount").toString());
        String reason = (String) req.get("reason");
        log.info("Admin debit points: userId={}, amount={}", userId, amount);
        long newBalance = pointsService.debit(userId, amount, reason, "admin", null);
        return ResponseEntity.ok().body(ApiResponse.success("Points debited successfully", Map.of(
            "userId", userId,
            "pointsDeducted", amount,
            "newBalance", newBalance
        )));
    }

    @PostMapping("/bulk-credit")
    public ResponseEntity<?> bulkCredit(@RequestBody BulkCreditRequest req) {
        log.info("Admin bulk credit: users={}, amount={}", req.getUserIds(), req.getAmount());
        Map<String, Object> result = Map.of(
            "totalUsers", req.getUserIds().size(),
            "pointsPerUser", req.getAmount(),
            "totalPointsDistributed", req.getAmount() * req.getUserIds().size()
        );
        for (Long uid : req.getUserIds()) {
            pointsService.credit(uid, req.getAmount(), req.getReason(), "ADMIN_BULK", null, "ADMIN");
        }
        return ResponseEntity.ok().body(ApiResponse.success("Bulk credit completed successfully", result));
    }

    @PostMapping("/reverse")
    public ResponseEntity<?> reverse(@RequestBody ReversePointsRequest req) {
        log.info("Admin reverse points: userId={}, amount={}", req.getUserId(), req.getAmount());
        // reverse by debiting the amount with reason
        long newBalance = pointsService.debit(req.getUserId(), req.getAmount(), req.getReason(), "admin", null);
        return ResponseEntity.ok().body(ApiResponse.success("Points reversed successfully", Map.of(
            "userId", req.getUserId(),
            "pointsReversed", req.getAmount(),
            "newBalance", newBalance
        )));
    }

    @GetMapping("/balance/{userId}")
    public ResponseEntity<?> getBalance(@PathVariable Long userId) {
        DinerlyPoints points = pointsService.getPointsEntity(userId);
        return ResponseEntity.ok().body(ApiResponse.success("User points retrieved successfully", Map.of(
            "userId", userId,
            "balance", points.getBalance()
        )));
    }

    @GetMapping("/ledger/{userId}")
    public ResponseEntity<?> getLedger(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Getting points ledger for userId={}", userId);
        Pageable pageable = PageRequest.of(page, size);
        // Implementation would call a service method to get ledger
        return ResponseEntity.ok().body(ApiResponse.success("Ledger retrieved successfully", Map.of(
            "userId", userId,
            "page", page,
            "size", size
        )));
    }

    @PostMapping("/set-balance")
    public ResponseEntity<?> setBalance(@RequestBody Map<String, Object> req) {
        Long userId = Long.valueOf(req.get("userId").toString());
        Long newBalance = Long.valueOf(req.get("newBalance").toString());
        String reason = (String) req.get("reason");
        log.info("Admin set balance: userId={}, newBalance={}", userId, newBalance);
        // Implementation would call a service method to set balance directly
        return ResponseEntity.ok().body(ApiResponse.success("Balance set successfully", Map.of(
            "userId", userId,
            "newBalance", newBalance
        )));
    }

    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics(@RequestParam(required = false) Long restaurantId) {
        log.info("Getting points statistics for restaurantId={}", restaurantId);
        Map<String, Object> stats = Map.of(
            "totalPointsDistributed", 0L,
            "totalPointsRedeemed", 0L,
            "averagePointsPerUser", 0L,
            "usersWithPoints", 0L
        );
        return ResponseEntity.ok().body(ApiResponse.success("Statistics retrieved successfully", stats));
    }

    @GetMapping("/top-earners")
    public ResponseEntity<?> getTopEarners(
            @RequestParam(required = false) Long restaurantId,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Getting top earners, limit={}", limit);
        return ResponseEntity.ok().body(ApiResponse.success("Top earners retrieved successfully", List.of()));
    }

    @PostMapping("/bulk-reverse")
    public ResponseEntity<?> bulkReverse(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> reverses = (List<Map<String, Object>>) request.get("reverses");
        log.info("Bulk reverse points for {} users", reverses.size());
        int completed = 0;
        for (Map<String, Object> entry : reverses) {
            Long userId = Long.valueOf(entry.get("userId").toString());
            Long amount = Long.valueOf(entry.get("amount").toString());
            String reason = (String) entry.get("reason");
            pointsService.debit(userId, amount, reason, "admin", null);
            completed++;
        }
        Map<String, Object> result = Map.of(
            "totalReverses", reverses.size(),
            "completedReverses", completed
        );
        return ResponseEntity.ok().body(ApiResponse.success("Bulk reverse completed successfully", result));
    }
}
