package com.restaurant.waitlist.backend.controller.admin;

import com.restaurant.waitlist.backend.dto.request.admin.CreditPointsRequest;
import com.restaurant.waitlist.backend.entity.DinerlyPoints;
import com.restaurant.waitlist.backend.repository.UserRepository;
import com.restaurant.waitlist.backend.service.PointsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        return ResponseEntity.ok().body(newBalance);
    }

    @PostMapping("/bulk-credit")
    public ResponseEntity<?> bulkCredit(@RequestBody com.restaurant.waitlist.backend.dto.request.admin.BulkCreditRequest req) {
        log.info("Admin bulk credit: users={}, amount={}", req.getUserIds(), req.getAmount());
        for (Long uid : req.getUserIds()) {
            pointsService.credit(uid, req.getAmount(), req.getReason(), "ADMIN_BULK", null, "admin");
        }
        return ResponseEntity.ok().body("Bulk credit initiated");
    }

    @PostMapping("/reverse")
    public ResponseEntity<?> reverse(@RequestBody com.restaurant.waitlist.backend.dto.request.admin.ReversePointsRequest req) {
        log.info("Admin reverse points: userId={}, amount={}", req.getUserId(), req.getAmount());
        // reverse by crediting back the amount with reason
        long newBalance = pointsService.credit(req.getUserId(), req.getAmount(), req.getReason(), "REVERSAL", req.getReferenceId(), "admin");
        return ResponseEntity.ok().body(newBalance);
    }
}
