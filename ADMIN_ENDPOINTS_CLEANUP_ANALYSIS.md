# Admin Rewards & Points Endpoints - Cleanup Analysis

## Current Controllers & Endpoints

### 1. **AdminRewardController** (`/api/admin/rewards`) ⚠️ NEEDS CLEANUP
**14 Endpoints** - Has REDUNDANT "Ways to Earn" and Settings

#### A. Tier Management (KEEP - CORE)
```
✅ GET    /api/admin/rewards/tiers
✅ POST   /api/admin/rewards/tiers
✅ PUT    /api/admin/rewards/tiers/{tierId}
✅ DELETE /api/admin/rewards/tiers/{tierId}
✅ GET    /api/admin/rewards/tiers/{tierId}
✅ PUT    /api/admin/rewards/tiers/{tierId}/duplicate
```

#### B. Ways to Earn (🔴 REMOVE - REDUNDANT)
```
❌ GET    /api/admin/rewards/ways-to-earn          [DUPLICATE: Use AdminPointsEarningRuleController instead]
❌ POST   /api/admin/rewards/ways-to-earn          [DUPLICATE: Use AdminPointsEarningRuleController instead]
❌ DELETE /api/admin/rewards/ways-to-earn/{ruleId} [DUPLICATE: Use AdminPointsEarningRuleController instead]
❌ PUT    /api/admin/rewards/ways-to-earn/{ruleId} [DUPLICATE: Use AdminPointsEarningRuleController instead]
```

#### C. Settings (⚠️ QUESTIONABLE)
```
❓ GET    /api/admin/rewards/settings
❓ PUT    /api/admin/rewards/settings
```
**Issue**: Generic settings - unclear what's stored. Should be clarified or merged.

#### D. Statistics (⚠️ CONSIDER MOVING)
```
❓ GET    /api/admin/rewards/statistics
❓ GET    /api/admin/rewards/user-tier-distribution
```
**Issue**: Statistics exists in multiple controllers. Should consolidate to one place.

---

### 2. **AdminRewardItemController** (`/api/admin/reward-items`) ✅ KEEP
**10 Endpoints** - No redundancy, all unique and necessary

```
✅ GET    /api/admin/reward-items                      [List all items]
✅ GET    /api/admin/reward-items/{itemId}             [Get item]
✅ POST   /api/admin/reward-items                      [Create item]
✅ PUT    /api/admin/reward-items/{itemId}             [Update item]
✅ DELETE /api/admin/reward-items/{itemId}             [Delete item]
✅ PUT    /api/admin/reward-items/{itemId}/toggle-availability
✅ POST   /api/admin/reward-items/bulk-toggle-availability
✅ GET    /api/admin/reward-items/category/{category}
✅ GET    /api/admin/reward-items/categories
✅ POST   /api/admin/reward-items/{itemId}/duplicate
```

---

### 3. **AdminPointsEarningRuleController** (`/api/admin/points/earning-rules`) ✅ KEEP
**10 Endpoints** - Handles earning rules (replaces AdminRewardController's ways-to-earn)

```
✅ GET    /api/admin/points/earning-rules
✅ GET    /api/admin/points/earning-rules/{ruleId}
✅ POST   /api/admin/points/earning-rules
✅ PUT    /api/admin/points/earning-rules/{ruleId}
✅ DELETE /api/admin/points/earning-rules/{ruleId}
✅ GET    /api/admin/points/earning-rules/by-action/{action}
✅ GET    /api/admin/points/earning-rules/actions
✅ PUT    /api/admin/points/earning-rules/{ruleId}/toggle-active
✅ POST   /api/admin/points/earning-rules/bulk-update-points
✅ GET    /api/admin/points/earning-rules/statistics
```

---

### 4. **AdminPointsController** (`/api/admin/points`) ✅ KEEP
**10 Endpoints** - Core points management

```
✅ POST   /api/admin/points/credit
✅ POST   /api/admin/points/debit
✅ POST   /api/admin/points/bulk-credit
✅ POST   /api/admin/points/reverse
✅ GET    /api/admin/points/balance/{userId}
✅ GET    /api/admin/points/ledger/{userId}
✅ POST   /api/admin/points/set-balance
✅ GET    /api/admin/points/statistics
✅ GET    /api/admin/points/top-earners
✅ POST   /api/admin/points/bulk-reverse
```

---

### 5. **AdminReceiptClaimController** (`/api/admin/receipt-claims`) ✅ KEEP
**15 Endpoints** - Unique receipt management

```
✅ GET    /api/admin/receipt-claims
✅ GET    /api/admin/receipt-claims/pending
✅ GET    /api/admin/receipt-claims/{claimId}
✅ GET    /api/admin/receipt-claims/{claimId}/details
✅ PUT    /api/admin/receipt-claims/{claimId}/approve
✅ PUT    /api/admin/receipt-claims/{claimId}/reject
✅ POST   /api/admin/receipt-claims/{claimId}/approve-bulk
✅ POST   /api/admin/receipt-claims/{claimId}/reject-bulk
✅ GET    /api/admin/receipt-claims/by-user/{userId}
✅ GET    /api/admin/receipt-claims/by-restaurant/{restaurantId}
✅ GET    /api/admin/receipt-claims/duplicates
✅ GET    /api/admin/receipt-claims/export
✅ GET    /api/admin/receipt-claims/statistics
✅ POST   /api/admin/receipt-claims/{claimId}/mark-duplicate
✅ POST   /api/admin/receipt-claims/{claimId}/revert
```

---

### 6. **AdminOfferController** (`/api/admin/offers`) ✅ KEEP
**17 Endpoints** - Unique offer management

```
✅ GET    /api/admin/offers
✅ GET    /api/admin/offers/{offerId}
✅ POST   /api/admin/offers
✅ PUT    /api/admin/offers/{offerId}
✅ DELETE /api/admin/offers/{offerId}
✅ PUT    /api/admin/offers/{offerId}/toggle-status
✅ POST   /api/admin/offers/{offerId}/duplicate
✅ POST   /api/admin/offers/bulk-duplicate
✅ PUT    /api/admin/offers/{offerId}/archive
✅ POST   /api/admin/offers/bulk-archive
✅ GET    /api/admin/offers/categories
✅ GET    /api/admin/offers/by-category/{category}
✅ GET    /api/admin/offers/expiring-soon
✅ GET    /api/admin/offers/low-inventory
✅ GET    /api/admin/offers/export
✅ GET    /api/admin/offers/statistics
✅ POST   /api/admin/offers/bulk-update-inventory
```

---

## 📋 Cleanup Recommendations

### **PRIORITY 1: Remove (Do Immediately)**

**Remove from AdminRewardController:**
1. ❌ `GET /api/admin/rewards/ways-to-earn` 
   - Use `GET /api/admin/points/earning-rules` instead

2. ❌ `POST /api/admin/rewards/ways-to-earn`
   - Use `POST /api/admin/points/earning-rules` instead

3. ❌ `PUT /api/admin/rewards/ways-to-earn/{ruleId}`
   - Use `PUT /api/admin/points/earning-rules/{ruleId}` instead

4. ❌ `DELETE /api/admin/rewards/ways-to-earn/{ruleId}`
   - Use `DELETE /api/admin/points/earning-rules/{ruleId}` instead

**Impact**: These 4 endpoints are 100% redundant with AdminPointsEarningRuleController

---

### **PRIORITY 2: Review (Clarify Purpose)**

**In AdminRewardController:**
1. ❓ `GET /api/admin/rewards/settings` 
   - What settings? For what purpose?
   - Decision: **Keep if it's reward program-level settings (e.g., global multipliers, expiration rules)**

2. ❓ `PUT /api/admin/rewards/settings`
   - Should this be split into separate endpoints? (e.g., `/settings/expiration`, `/settings/multipliers`)
   - Decision: **Consolidate into one focused settings endpoint**

3. ❓ `GET /api/admin/rewards/statistics`
   - Overlaps with `/api/admin/points/statistics`
   - Decision: **Remove - use points statistics instead**

4. ❓ `GET /api/admin/rewards/user-tier-distribution`
   - Unique analytics view showing user distribution across tiers
   - Decision: **Keep if different from points statistics; consider moving to analytics endpoint**

---

### **PRIORITY 3: Monitor (No Action Needed)**

**These are fine:**
- AdminRewardItemController (10 endpoints) - Core reward catalog
- AdminPointsEarningRuleController (10 endpoints) - Earning rules engine
- AdminPointsController (10 endpoints) - Points ledger & operations
- AdminReceiptClaimController (15 endpoints) - Receipt verification workflow
- AdminOfferController (17 endpoints) - Offer management

---

## 📊 Summary Table

| Controller | Endpoints | Status | Action |
|-----------|-----------|--------|--------|
| AdminRewardController | 14 | ⚠️ NEEDS CLEANUP | Remove 4 "ways-to-earn" endpoints |
| AdminRewardItemController | 10 | ✅ GOOD | No changes |
| AdminPointsEarningRuleController | 10 | ✅ GOOD | No changes |
| AdminPointsController | 10 | ✅ GOOD | No changes |
| AdminReceiptClaimController | 15 | ✅ GOOD | No changes |
| AdminOfferController | 17 | ✅ GOOD | No changes |
| **TOTAL** | **76** | → 72 after cleanup | Remove 4 redundant endpoints |

---

## Files to Modify

### **AdminRewardController.java**
**Delete lines 69-119** (Ways to Earn section):
```java
@GetMapping("/ways-to-earn")
public ResponseEntity<ApiResponse<List<WayToEarnRequest>>> waysToEarn() { ... }

@PostMapping("/ways-to-earn")
public ResponseEntity<ApiResponse<WayToEarnRequest>> createWayToEarn(...) { ... }

@DeleteMapping("/ways-to-earn/{ruleId}")
public ResponseEntity<ApiResponse<Object>> deleteWayToEarn(...) { ... }

@PutMapping("/ways-to-earn/{ruleId}")
public ResponseEntity<ApiResponse<WayToEarnRequest>> updateWayToEarn(...) { ... }
```

### **AdminRewardService.java**
**Delete 4 methods:**
- `listWaysToEarn()`
- `createWayToEarn(request)`
- `deleteWayToEarn(ruleId)`
- `updateWayToEarn(ruleId, request)`

### **AdminRewardServiceImpl.java**
**Delete implementations of the 4 methods above**

---

## Endpoints After Cleanup

### **AdminRewardController** (Cleaned) - 10 Endpoints
```
✅ GET    /api/admin/rewards/tiers
✅ POST   /api/admin/rewards/tiers
✅ PUT    /api/admin/rewards/tiers/{tierId}
✅ DELETE /api/admin/rewards/tiers/{tierId}
✅ GET    /api/admin/rewards/tiers/{tierId}
✅ PUT    /api/admin/rewards/tiers/{tierId}/duplicate
✅ GET    /api/admin/rewards/settings
✅ PUT    /api/admin/rewards/settings
✅ GET    /api/admin/rewards/statistics
✅ GET    /api/admin/rewards/user-tier-distribution
```

### **AdminPointsEarningRuleController** (Replaces) - 10 Endpoints
```
✅ GET    /api/admin/points/earning-rules                    [Replaces: ways-to-earn]
✅ GET    /api/admin/points/earning-rules/{ruleId}
✅ POST   /api/admin/points/earning-rules
✅ PUT    /api/admin/points/earning-rules/{ruleId}
✅ DELETE /api/admin/points/earning-rules/{ruleId}
✅ GET    /api/admin/points/earning-rules/by-action/{action}
✅ GET    /api/admin/points/earning-rules/actions
✅ PUT    /api/admin/points/earning-rules/{ruleId}/toggle-active
✅ POST   /api/admin/points/earning-rules/bulk-update-points
✅ GET    /api/admin/points/earning-rules/statistics
```

---

## Migration Guide for Clients

**Old Endpoints → New Endpoints**

| Old | New |
|-----|-----|
| `GET /api/admin/rewards/ways-to-earn` | `GET /api/admin/points/earning-rules` |
| `POST /api/admin/rewards/ways-to-earn` | `POST /api/admin/points/earning-rules` |
| `PUT /api/admin/rewards/ways-to-earn/{id}` | `PUT /api/admin/points/earning-rules/{id}` |
| `DELETE /api/admin/rewards/ways-to-earn/{id}` | `DELETE /api/admin/points/earning-rules/{id}` |

---

## Proceed with Cleanup?

**Recommended Action:**
1. ✅ Remove 4 "ways-to-earn" endpoints from AdminRewardController
2. ✅ Update documentation to point to AdminPointsEarningRuleController
3. ✅ Keep reward tiers, items, offers, points management separate
4. ⚠️ Review "settings" endpoint - may need consolidation

**Do you want me to proceed with the cleanup?** (Yes/No)

