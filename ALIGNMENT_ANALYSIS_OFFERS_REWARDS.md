# Offers & Rewards — Requirements Alignment Analysis

**Date:** 2026-09-16  
**Status:** REQUIRES SIGNIFICANT CHANGES  
**Overall Alignment:** 30% Complete (Admin backend only, Guest APIs missing entirely)

---

## Executive Summary

The backend has **basic Offer and Points infrastructure** but is **significantly misaligned** with the functional requirements from the 3 specification documents:

1. ✅ Admin Offer CRUD (partial)
2. ❌ Guest Offers API (MISSING)
3. ❌ Guest Rewards API (MISSING)
4. ❌ Redemption codes (MISSING)
5. ❌ Rewards tiers & configuration (MISSING)
6. ❌ Receipt scanning (MISSING)
7. ❌ Advanced offer features (MISSING)

---

## Detailed Gap Analysis

### 1. GUEST OFFERS PAGE — BACKEND API ENDPOINTS

**Requirement Status:** ❌ NOT IMPLEMENTED

**Required Endpoints:**

| Endpoint | Purpose | Status | Notes |
|----------|---------|--------|-------|
| `GET /api/offers?locationId={id}` | List active coupons for location | ❌ Missing | Must return filtered, formatted for coupon grid |
| `GET /api/offers/{offerId}` | Get single offer details | ❌ Missing | Pre-fill for redemption modal |
| `POST /api/offers/{offerId}/redeem` | Redeem coupon & generate code | ❌ Missing | **CRITICAL** — Must generate 6-digit code |
| `GET /api/offers/{offerId}/categories` | Filter by % off / $ off / Rewards eligible | ❌ Missing | Need category filter support |

**Missing Data Model Features:**

| Field | Current State | Required | Impact |
|-------|---------------|----------|--------|
| Discount type | ❌ No | % off, $ off, Free item, Points eligible | Guests can't identify offer type |
| Discount amount | ❌ No | e.g., "23% off", "$3 off", "Kids free" | Badge display broken |
| Offer description | ❌ Partial (name only) | Full description in modal | UX missing |
| Photo/Image | ❌ No | Food photo reference | Grid display broken |
| Rating | ❌ No | Star rating on card | Social proof missing |
| Restriction tags | ❌ No | e.g., "Dine-in only", "Sat-Sun only" | Legal compliance missing |
| Offer category | ❌ No | % off, $ off, Rewards eligible | Filtering broken |
| Per-offer limits | ❌ No | Max redemptions per user/day | Fraud protection missing |
| Inventory | ⚠️ Partial | Needed for coupons | Stock tracking |
| Original price | ❌ No | For strikethrough display | UX requirement |

**Missing Response DTO:**

```java
// Currently: OfferResponse (admin-focused)
// Needed: GuestOfferResponse
{
  "id": 1,
  "name": "Classic Breakfast Combo",
  "description": "Eggs, toast, and coffee",
  "location": {
    "id": 1,
    "name": "Brothers Café",
    "distance": "0.3 mi"
  },
  "discountType": "PERCENT",  // PERCENT, FIXED, FREE_ITEM, REWARDS_ELIGIBLE
  "discountValue": 23,
  "discountLabel": "23% off",
  "originalPrice": 12.99,
  "currentPrice": 9.99,
  "photo": "https://...",
  "rating": 4.7,
  "restrictions": ["Dine-in only"],
  "category": "% off",  // For filtering
  "redeemable": true,  // Computed if user authenticated
  "userCanRedeem": true,
  "reasonIfNotRedeemable": "Already redeemed 2 times today"
}
```

---

### 2. GUEST OFFERS REDEMPTION FLOW

**Requirement Status:** ❌ CRITICAL MISSING — 6-Digit Redemption Code

**Current Implementation:**

```java
// Redemption entity has NO code field
@Entity
public class Redemption {
    private Long id;
    private Offer offer;
    private Long restaurantId;
    private String guestName;
    private String guestPhone;
    private LocalDateTime redeemedAt;
    private BigDecimal value;
    // ❌ MISSING: String redemptionCode (6-digit)
    // ❌ MISSING: LocalDateTime codeExpiry
    // ❌ MISSING: String status (REQUESTED/COMPLETED/REDEEMED)
}
```

**Required Behavior (from spec):**

1. Guest clicks "Redeem" on coupon card
2. Confirmation modal opens with:
   - Coupon name, price, restaurant name
   - Disclaimer: "shown to staff in person, cannot be undone"
   - "Confirm redeem" button
3. On confirm:
   - ✅ Generate random 6-digit numeric code
   - ✅ Display code in modal with "Show this code to your server"
   - ✅ Create Redemption record in DB
   - ✅ Send SMS/notification to guest (optional)
4. Guest shows code to staff who enters it on POS

**What's Missing:**

- No code generation logic
- No code validation endpoint
- No code expiry logic
- No status transitions (REQUESTED → COMPLETED)
- No code redemption confirmation by staff

**Required New Endpoint:**

```
POST /api/offers/{offerId}/redeem
Response: {
  "redemptionCode": "514527",  // 6-digit random
  "expiresAt": "2026-09-16T14:30:00Z",  // 1 hour TTL
  "message": "Show this code to your server"
}

POST /api/offers/redeem/{code}/confirm  // Staff validates on POS
Response: { "status": "COMPLETED" }
```

---

### 3. GUEST REWARDS PAGE — BACKEND API ENDPOINTS

**Requirement Status:** ❌ NOT IMPLEMENTED

**Required Endpoints:**

| Endpoint | Purpose | Status | Notes |
|----------|---------|--------|-------|
| `GET /api/rewards/profile` | Get current points, tier, progress | ❌ Missing | Board-pass card data |
| `GET /api/rewards/redeemable` | List redeem-able rewards | ❌ Missing | Filtered by affordability |
| `POST /api/rewards/{rewardId}/redeem` | Redeem reward with points | ❌ Missing | Deduct points, generate code |
| `POST /api/rewards/receipt/claim` | Upload receipt & claim points | ❌ Missing | Multipart file upload |
| `GET /api/rewards/ways-to-earn` | Get earning opportunities | ❌ Missing | Static list of actions |

**Missing Data Model — Rewards Tiers & Config:**

```java
// ❌ COMPLETELY MISSING
@Entity
public class RewardTier {
    private Long id;
    private String name;  // Silver, Gold, Platinum
    private Long pointsThreshold;  // Min points for tier
    private String color;
    private List<String> perks;
    private Long restaurantId;
}

@Entity
public class RewardItem {
    private Long id;
    private String title;  // "Free coffee"
    private String description;
    private Long pointsCost;
    private Long restaurantId;
    private String category;  // Food, beverage, discount
}

@Entity  // For "Ways to Earn"
public class PointsEarningRule {
    private Long id;
    private String action;  // "dine_in", "join_waitlist", "leave_review", "refer_friend"
    private Long pointsValue;
    private String description;
    private Long restaurantId;
}
```

**Missing Response DTO — Guest Rewards Profile:**

```java
{
  "currentPoints": 240,
  "currentTier": {
    "name": "Silver tier",
    "color": "silver",
    "pointsThreshold": 0
  },
  "tierProgress": {
    "pointsToNextTier": 110,  // 350 - 240
    "nextTierName": "Gold",
    "progressPercentage": 68.6  // 240 / 350
  },
  "redeemableRewards": [
    {
      "id": 1,
      "title": "Free coffee",
      "description": "Any size, any blend",
      "pointsCost": 100,
      "icon": "coffee",
      "status": "REDEEMABLE"
    },
    {
      "id": 2,
      "title": "$5 off your bill",
      "description": "Valid on any day",
      "pointsCost": 150,
      "icon": "discount",
      "status": "REDEEMABLE"
    },
    {
      "id": 3,
      "title": "Free dessert",
      "description": "Any item from dessert menu",
      "pointsCost": 300,
      "icon": "dessert",
      "status": "NOT_ENOUGH_POINTS"  // 240 < 300
    }
  ],
  "waysToEarn": [
    {
      "action": "dine_in",
      "title": "Dined without joining a waitlist?",
      "subtitle": "Show your receipt code to claim points",
      "pointsValue": 15,
      "clickable": true,
      "action": "OPEN_RECEIPT_MODAL"
    },
    {
      "action": "join_waitlist",
      "title": "Join the waitlist",
      "subtitle": "Points added automatically when you dine",
      "pointsValue": 10,
      "clickable": false
    },
    // ... more
  ]
}
```

---

### 4. RECEIPT SCANNING FEATURE

**Requirement Status:** ❌ NOT IMPLEMENTED

**Required Functionality:**

1. Guest uploads receipt image (camera scan or file browse)
2. Backend processes image:
   - ❌ OCR/validation (date, total, restaurant name visible)
   - ❌ Duplicate detection (prevent same receipt claimed twice)
   - ❌ Minimum spend validation (e.g., >= $10)
3. Award points on approval
4. Create PointsLedger entry with receipt reference

**Required Endpoint:**

```
POST /api/rewards/receipt/claim
Content-Type: multipart/form-data
- file: [image]
- restaurantId: 1

Response: {
  "pointsClaimed": 15,
  "newBalance": 255,
  "receiptReference": "RCP-20260916-001",
  "status": "APPROVED"  // or PENDING_REVIEW
}
```

**What's Missing:**

- No file upload endpoint
- No image processing/OCR
- No duplicate receipt detection
- No admin approval workflow
- No receipt history

---

### 5. OFFER MODEL — MISALIGNMENT WITH REQUIREMENTS

**Current Offer Entity:**

```java
@Entity
public class Offer {
    private Long id;
    private String name;
    private Restaurant restaurant;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;  // DRAFT, INACTIVE, Active
    private BigDecimal value;
    private Long pointsCost;
    // ❌ MISSING 9+ fields
}
```

**Required Fields (per Guest Offers spec):**

| Field | Type | Purpose | Currently |
|-------|------|---------|-----------|
| `discountType` | ENUM | % off, $ off, Free item, Rewards eligible | ❌ No |
| `discountValue` | Decimal | 23.5 for "23.5% off", 5 for "$5 off" | ⚠️ In `value` but no type |
| `discountLabel` | String | "23% off", "$3 off", "Kids free" (display) | ❌ No |
| `description` | Text | Offer details for modal | ❌ No |
| `restrictions` | Array/String | "Dine-in only", "Sat-Sun only" | ❌ No |
| `photo` | String (URL/FK) | Food image for card | ❌ No |
| `rating` | Double | Guest rating (4.7 ★) | ❌ No |
| `ratingCount` | Long | Number of reviews | ❌ No |
| `perUserLimit` | Integer | Max redemptions per user (e.g., 2) | ❌ No |
| `perUserDailyLimit` | Integer | Max per user per day | ❌ No |
| `inventory` | Integer | Coupons available | ⚠️ Not in current model |
| `originalPrice` | Decimal | For strikethrough | ❌ No |
| `category` | String | "% off", "$ off", "Rewards eligible" (for filtering) | ❌ No |

---

### 6. REWARDS TIER SYSTEM — COMPLETELY MISSING

**Requirement (from Admin Portal spec, Section 6):**

```
Loyalty tier configuration: Silver / Gold / Platinum
- Point thresholds (e.g., Silver: 0-349, Gold: 350-699, Platinum: 700+)
- Perks per tier
- "Ways to earn" configuration
- Redemption catalog
```

**Current State:** ❌ ZERO implementation

**What's Missing:**

1. **Tier Configuration:**
   - No `RewardTier` entity
   - No tier hierarchy
   - No point thresholds
   - No perks definition

2. **Rewards Catalog:**
   - No `RewardItem` entity for admin to create rewards
   - No admin endpoint to manage rewards
   - No "Ways to earn" configuration

3. **Guest API:**
   - No endpoint to fetch current tier
   - No endpoint to fetch tier progress
   - No endpoint to list redeemable items

**Example Missing Admin Endpoint:**

```
POST /api/admin/rewards/tiers
Body: {
  "name": "Gold",
  "pointsThreshold": 350,
  "perks": ["Free dessert", "Priority seating", "10% discount"]
}

POST /api/admin/rewards/items
Body: {
  "title": "Free coffee",
  "pointsCost": 100,
  "restaurantId": 1
}

POST /api/admin/rewards/earning-rules
Body: {
  "action": "join_waitlist",
  "pointsValue": 10,
  "description": "Points added automatically when you dine"
}
```

---

### 7. OFFER CATEGORY FILTERING

**Requirement (Guest Offers spec, Section 4):**

```
Filter chips: "% off", "$ off", "Rewards eligible"
Selecting a chip filters the coupon grid to matching offers.
```

**Current State:** ❌ NOT IMPLEMENTED

**What's Missing:**

- No category field on Offer entity
- No filtering by category in repository
- No guest endpoint with category filtering
- No response DTO with category field

**Required Repository Method:**

```java
@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {
    Page<Offer> findByRestaurantIdAndCategoryAndStatusAndDateRange(
        Long restaurantId, 
        String category,  // "PERCENT", "FIXED", "FREE_ITEM", "REWARDS_ELIGIBLE"
        String status,
        LocalDate startDate,
        LocalDate endDate,
        Pageable pageable
    );
}
```

---

## Summary of Required Fixes & Additions

### Phase 1: Offer Model & Admin (2-3 days)

**Database Migrations:**
- Add fields to `Offer`: discountType, discountValue, discountLabel, description, restrictions[], photoUrl, rating, ratingCount, perUserLimit, perUserDailyLimit, inventory, originalPrice, category

**Entity Changes:**
```java
@Entity
public class Offer {
    // Existing
    private Long id;
    private String name;
    private Restaurant restaurant;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    
    // NEW
    @Enumerated
    private DiscountType discountType;  // PERCENT, FIXED, FREE_ITEM, REWARDS_ELIGIBLE
    private BigDecimal discountValue;
    private String discountLabel;  // "23% off"
    private String description;
    @ElementCollection
    private List<String> restrictions;  // ["Dine-in only", "Sat-Sun only"]
    private String photoUrl;
    private Double rating;
    private Long ratingCount;
    private Integer perUserLimit;
    private Integer perUserDailyLimit;
    private Integer inventory;
    private BigDecimal originalPrice;
    private String category;  // PERCENT, FIXED, REWARDS_ELIGIBLE
}
```

**Admin API Enhancements:**
- Update `OfferRequest` DTO with new fields
- Update `OfferResponse` to include category
- Add category filtering to `listOffers()`

---

### Phase 2: Guest Offers API (3-4 days)

**New Endpoints:**
- `GET /api/offers?locationId={id}&category={cat}` — List offers with filtering
- `GET /api/offers/{offerId}` — Get offer details
- `POST /api/offers/{offerId}/redeem` — Redeem offer & generate 6-digit code

**New DTOs:**
- `GuestOfferResponse` (with category, photo, rating, restrictions, redeemable flag)
- `RedeemOfferRequest` / `RedeemOfferResponse` (with 6-digit code)

**Redemption Changes:**
```java
@Entity
public class Redemption {
    // Existing
    private Long id;
    private Offer offer;
    private Long restaurantId;
    private String guestName;
    private String guestPhone;
    private LocalDateTime redeemedAt;
    private BigDecimal value;
    
    // NEW
    @Column(unique = true, nullable = false)
    private String redemptionCode;  // 6-digit unique code
    private LocalDateTime codeExpiresAt;
    @Enumerated
    private RedemptionStatus status;  // GENERATED, COMPLETED, EXPIRED
    private Long userId;  // Track which guest redeemed
}

enum RedemptionStatus {
    GENERATED,     // Code generated, shown to guest
    COMPLETED,     // Staff entered code, redeemed
    EXPIRED,       // Code TTL exceeded
    CANCELLED
}
```

**Code Generation Utility:**
```java
public class RedemptionCodeGenerator {
    public static String generate() {
        return String.format("%06d", new Random().nextInt(1000000));
    }
}
```

---

### Phase 3: Rewards Program Setup (2-3 days)

**New Entities:**
```java
@Entity
public class RewardTier {
    private Long id;
    private Long restaurantId;
    private String name;  // Silver, Gold, Platinum
    private Long pointsThreshold;
    private String color;
    @ElementCollection
    private List<String> perks;
    private Integer tier_order;  // 1, 2, 3
}

@Entity
public class RewardItem {
    private Long id;
    private Long restaurantId;
    private String title;  // "Free coffee"
    private String description;
    private Long pointsCost;
    private String icon;
    private String category;
}

@Entity
public class PointsEarningRule {
    private Long id;
    private Long restaurantId;
    private String action;  // dine_in, join_waitlist, leave_review, refer_friend
    private Long pointsValue;
    private String description;
    private String icon;
    private Boolean clickable;  // If true, opens action flow
}
```

**Admin Endpoints:**
- `POST /api/admin/rewards/tiers` — Create tier
- `GET /api/admin/rewards/tiers` — List tiers
- `POST /api/admin/rewards/items` — Create reward item
- `GET /api/admin/rewards/items` — List items
- `POST /api/admin/rewards/earning-rules` — Configure earning rules

---

### Phase 4: Guest Rewards API (3-4 days)

**New Endpoints:**
- `GET /api/rewards/profile` — Current points, tier, progress
- `GET /api/rewards/redeemable` — List redeemable rewards
- `POST /api/rewards/{itemId}/redeem` — Redeem with points deduction & code
- `GET /api/rewards/ways-to-earn` — Earning opportunities
- `POST /api/rewards/receipt/claim` — Upload receipt image & claim points

**New DTOs:**
- `GuestRewardsProfileResponse` (points, tier, progress)
- `RedeemRewardResponse` (with 6-digit code)
- `ClaimReceiptResponse` (points awarded, new balance)

---

### Phase 5: Receipt Scanning (2-3 days)

**New Entity:**
```java
@Entity
public class ReceiptClaim {
    private Long id;
    private Long userId;
    private Long restaurantId;
    private String fileUrl;  // S3 / Cloud Storage path
    private Long pointsClaimed;
    @Enumerated
    private ClaimStatus status;  // UPLOADED, APPROVED, REJECTED
    private String rejectionReason;
    private LocalDateTime createdAt;
}

enum ClaimStatus {
    UPLOADED,   // Waiting for approval
    APPROVED,   // Points credited
    REJECTED,   // Rejected by admin
    DUPLICATE   // Already claimed
}
```

**Required Functionality:**
- File upload to S3/Cloud Storage
- Duplicate detection (same date + amount)
- Admin approval workflow
- OCR validation (optional, can be manual)

---

## Impact on Existing Code

### Files to Modify:

1. **Database Migrations:**
   - Create migration for Offer fields
   - Create migration for RewardTier, RewardItem, PointsEarningRule
   - Create migration for Redemption.redemptionCode, .status
   - Create migration for ReceiptClaim table

2. **Entity Classes:**
   - Offer.java (add 10+ fields)
   - Redemption.java (add 3 fields)
   - Create RewardTier.java, RewardItem.java, PointsEarningRule.java, ReceiptClaim.java

3. **Controllers:**
   - Existing: AdminOfferController.java
   - New: GuestOfferController.java, GuestRewardsController.java, AdminRewardController.java

4. **Services:**
   - Modify: AdminOfferService/Impl
   - New: GuestOfferService/Impl, GuestRewardsService/Impl, AdminRewardService/Impl, ReceiptClaimService/Impl

5. **Repositories:**
   - Modify: OfferRepository (add filtering)
   - New: RewardTierRepository, RewardItemRepository, PointsEarningRuleRepository, ReceiptClaimRepository

6. **DTOs:**
   - Modify: OfferRequest, OfferResponse
   - New: GuestOfferResponse, RedeemOfferResponse, GuestRewardsProfileResponse, RedeemRewardResponse, etc.

---

## Risk Assessment

| Area | Risk Level | Impact |
|------|-----------|--------|
| Offer model expansion | 🟡 Medium | Database migration complexity, backward compat |
| 6-digit code uniqueness | 🔴 High | Must ensure no collisions, add unique constraint |
| Redemption code TTL | 🟡 Medium | Need background job to clean expired codes |
| Receipt file handling | 🟡 Medium | S3/Cloud storage integration, file size limits |
| Points tier calculation | 🟢 Low | Simple aggregation logic |
| Concurrent redemptions | 🟡 Medium | Need optimistic locking on inventory |

---

## Timeline Estimate

| Phase | Duration | Depends On |
|-------|----------|-----------|
| Phase 1: Offer Model | 2-3 days | — |
| Phase 2: Guest Offers API | 3-4 days | Phase 1 |
| Phase 3: Rewards Tiers & Config | 2-3 days | Phase 1 |
| Phase 4: Guest Rewards API | 3-4 days | Phase 3 |
| Phase 5: Receipt Scanning | 2-3 days | Phase 4 |
| **Total** | **12-17 days** | Sequential |

**Parallel tracks possible:**
- Phase 1 + 3 can run in parallel (2-3 days for both)
- Phase 2 and 4 can run in parallel (3-4 days for both)
- Phase 5 is independent (2-3 days)
- **Optimized: 7-10 days** if parallelized

---

## Recommended Next Steps

1. ✅ Review this analysis with product/design
2. ✅ Confirm offer fields (discount types, restrictions, etc.)
3. ✅ Confirm tier names and point thresholds
4. ✅ Confirm "ways to earn" rules
5. ✅ Finalize redemption code TTL (1 hour? 30 min?)
6. ✅ Decide on receipt approval workflow (auto vs manual)
7. ✅ Update REFACTORING_AND_OPTIMIZATION_PLAN.md with these items
8. ✅ Begin Phase 1 implementation

---

**Generated by:** Architecture Review  
**Last Updated:** 2026-09-16
