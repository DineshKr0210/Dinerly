# Complete Admin API Endpoints Documentation

## Overview
This document provides comprehensive details on all admin API endpoints for managing offers, rewards, points, and redemptions.

---

## Admin Offer API Endpoints

### Base URL: `/api/admin/offers`

#### List Offers
```
GET /api/admin/offers
Query Parameters:
  - locationId (Long, optional)
  - status (String, optional): ACTIVE, INACTIVE, EXPIRED
  - category (String, optional): PERCENT, FIXED, FREE_ITEM, REWARDS_ELIGIBLE
  - page (int, default: 0)
  - size (int, default: 20)
Response: Page<OfferResponse>
```

#### Get Offer Details
```
GET /api/admin/offers/{offerId}
Response: OfferResponse
```

#### Create Offer
```
POST /api/admin/offers
Body: OfferRequest {
  name, startDate, endDate, status, value,
  discountType, discountLabel, description,
  restrictions, photoUrl, rating, category,
  perUserLimit, perUserDailyLimit, inventory
}
Response: OfferResponse
```

#### Update Offer
```
PUT /api/admin/offers/{offerId}
Body: OfferRequest (same as create)
Response: OfferResponse
```

#### Delete Offer
```
DELETE /api/admin/offers/{offerId}
Response: Success message
```

#### Toggle Offer Status
```
PUT /api/admin/offers/{offerId}/toggle-status
Response: OfferResponse (with updated status)
```

#### Duplicate Offer
```
POST /api/admin/offers/{offerId}/duplicate?newName=optional
Response: OfferResponse (new duplicated offer)
```

#### Bulk Duplicate Offers
```
POST /api/admin/offers/bulk-duplicate
Body: {
  "offerIds": [1, 2, 3]
}
Response: { "totalDuplicated": 3, "offers": [...] }
```

#### Archive Offer
```
PUT /api/admin/offers/{offerId}/archive
Response: OfferResponse (archived)
```

#### Bulk Archive Offers
```
POST /api/admin/offers/bulk-archive
Body: { "offerIds": [1, 2, 3] }
Response: { "totalArchived": 3 }
```

#### Get Offer Categories
```
GET /api/admin/offers/categories?locationId=optional
Response: List<String> ["PERCENT", "FIXED", ...]
```

#### Get Offers by Category
```
GET /api/admin/offers/by-category/{category}?locationId=optional&page=0&size=20
Response: Page<OfferResponse>
```

#### Get Expiring Soon Offers
```
GET /api/admin/offers/expiring-soon?locationId=optional&days=7&page=0&size=20
Response: Page<OfferResponse>
```

#### Get Low Inventory Offers
```
GET /api/admin/offers/low-inventory?locationId=optional&threshold=10&page=0&size=20
Response: Page<OfferResponse>
```

#### Export Offers to CSV
```
GET /api/admin/offers/export?locationId=optional&status=optional&from=optional&to=optional
Response: CSV file (attachment)
```

#### Get Offer Statistics
```
GET /api/admin/offers/statistics?locationId=optional
Response: {
  "totalOffers": 50,
  "activeOffers": 35,
  "expiredOffers": 15,
  "totalRedemptions": 1200,
  "averageRedemptionsPerOffer": 24
}
```

#### Bulk Update Inventory
```
POST /api/admin/offers/bulk-update-inventory
Body: {
  "updates": [
    { "offerId": 1, "newInventory": 100 },
    { "offerId": 2, "newInventory": 50 }
  ]
}
Response: { "updated": 2, "total": 2 }
```

---

## Admin Reward Items API Endpoints

### Base URL: `/api/admin/reward-items`

#### List Reward Items
```
GET /api/admin/reward-items
Query Parameters:
  - restaurantId (Long, optional)
  - category (String, optional)
  - available (Boolean, optional)
  - page (int, default: 0)
  - size (int, default: 20)
Response: Page<RewardItemResponse>
```

#### Get Reward Item Details
```
GET /api/admin/reward-items/{itemId}
Response: RewardItemResponse
```

#### Create Reward Item
```
POST /api/admin/reward-items
Body: RewardItemRequest {
  title, description, pointsCost, restaurantId,
  icon, category, available
}
Response: RewardItemResponse
```

#### Update Reward Item
```
PUT /api/admin/reward-items/{itemId}
Body: RewardItemRequest
Response: RewardItemResponse
```

#### Delete Reward Item
```
DELETE /api/admin/reward-items/{itemId}
Response: Success message
```

#### Toggle Item Availability
```
PUT /api/admin/reward-items/{itemId}/toggle-availability
Response: RewardItemResponse
```

#### Bulk Toggle Availability
```
POST /api/admin/reward-items/bulk-toggle-availability
Body: {
  "itemIds": [1, 2, 3],
  "available": true
}
Response: { "totalRequested": 3, "updated": 3 }
```

#### Get Items by Category
```
GET /api/admin/reward-items/category/{category}?restaurantId=optional&page=0&size=20
Response: Page<RewardItemResponse>
```

#### Get Available Categories
```
GET /api/admin/reward-items/categories?restaurantId=optional
Response: List<String> ["beverage", "food", "dessert", "special"]
```

#### Duplicate Reward Item
```
POST /api/admin/reward-items/{itemId}/duplicate?newTitle=optional
Response: RewardItemResponse
```

---

## Admin Points Earning Rules API Endpoints

### Base URL: `/api/admin/points/earning-rules`

#### List Earning Rules
```
GET /api/admin/points/earning-rules
Query Parameters:
  - restaurantId (Long, optional)
  - action (String, optional)
  - page (int, default: 0)
  - size (int, default: 20)
Response: Page<PointsEarningRuleResponse>
```

#### Get Earning Rule Details
```
GET /api/admin/points/earning-rules/{ruleId}
Response: PointsEarningRuleResponse
```

#### Create Earning Rule
```
POST /api/admin/points/earning-rules
Body: PointsEarningRuleRequest {
  action, pointsValue, restaurantId, description,
  icon, clickable, actionUrl
}
Response: PointsEarningRuleResponse
```

#### Update Earning Rule
```
PUT /api/admin/points/earning-rules/{ruleId}
Body: PointsEarningRuleRequest
Response: PointsEarningRuleResponse
```

#### Delete Earning Rule
```
DELETE /api/admin/points/earning-rules/{ruleId}
Response: Success message
```

#### Get Rules by Action
```
GET /api/admin/points/earning-rules/by-action/{action}?restaurantId=optional
Response: List<PointsEarningRuleResponse>
```

#### Get Available Actions
```
GET /api/admin/points/earning-rules/actions?restaurantId=optional
Response: [
  {
    "action": "dine_in",
    "display": "Dined in restaurant",
    "default": 15
  },
  ...
]
```

#### Toggle Rule Active Status
```
PUT /api/admin/points/earning-rules/{ruleId}/toggle-active
Response: PointsEarningRuleResponse
```

#### Bulk Update Points Values
```
POST /api/admin/points/earning-rules/bulk-update-points
Body: {
  "updates": [
    { "ruleId": 1, "newPoints": 20 },
    { "ruleId": 2, "newPoints": 50 }
  ]
}
Response: { "totalRequested": 2, "updated": 2 }
```

#### Get Points Statistics
```
GET /api/admin/points/earning-rules/statistics?restaurantId=optional
Response: {
  "totalRules": 5,
  "activeRules": 4,
  "totalPointsDistributed": 50000,
  "averagePointsPerRule": 10000
}
```

---

## Admin Redemption API Endpoints

### Base URL: `/api/admin/redemptions`

#### List Redemptions
```
GET /api/admin/redemptions
Query Parameters:
  - locationId (Long, optional)
  - status (String, optional): GENERATED, COMPLETED, EXPIRED, CANCELLED
  - from (LocalDateTime, optional, format: 2026-09-16T10:00:00)
  - to (LocalDateTime, optional)
  - page (int, default: 0)
  - size (int, default: 20)
Response: Page<RedemptionResponse>
```

#### Get Redemption by ID
```
GET /api/admin/redemptions/{redemptionId}
Response: RedemptionResponse
```

#### Get Redemption by Code
```
GET /api/admin/redemptions/by-code/{code}
Response: RedemptionResponse
```

#### Get Redemptions by Status
```
GET /api/admin/redemptions/by-status/{status}?locationId=optional&page=0&size=20
Response: Page<RedemptionResponse>
```

#### Get Expired Codes
```
GET /api/admin/redemptions/expired-codes?locationId=optional&page=0&size=20
Response: Page<RedemptionResponse>
```

#### Cancel Redemption
```
PUT /api/admin/redemptions/{redemptionId}/cancel?reason=optional
Response: RedemptionResponse (status=CANCELLED)
```

#### Expire Code
```
PUT /api/admin/redemptions/{redemptionId}/expire
Response: RedemptionResponse (status=EXPIRED)
```

#### Bulk Expire Codes
```
POST /api/admin/redemptions/expire-bulk
Body: {
  "redemptionIds": [1, 2, 3],
  "reason": "System maintenance"
}
Response: { "totalRequested": 3, "expired": 3 }
```

#### Export Redemptions to CSV
```
GET /api/admin/redemptions/export?locationId=optional&status=optional&from=optional&to=optional
Response: CSV file (attachment)
```

#### Get Redemption Statistics
```
GET /api/admin/redemptions/statistics?locationId=optional&from=optional&to=optional
Response: {
  "totalRedemptions": 1200,
  "completedRedemptions": 1100,
  "expiredCodes": 80,
  "cancelledRedemptions": 20,
  "averageRedemptionValue": 25.50
}
```

#### Get Redemptions by Offer
```
GET /api/admin/redemptions/by-offer/{offerId}?page=0&size=20
Response: Page<RedemptionResponse>
```

#### Get Redemptions by User
```
GET /api/admin/redemptions/by-user/{userId}?page=0&size=20
Response: Page<RedemptionResponse>
```

---

## Admin Receipt Claims API Endpoints

### Base URL: `/api/admin/receipt-claims`

#### List Receipt Claims
```
GET /api/admin/receipt-claims
Query Parameters:
  - restaurantId (Long, optional)
  - status (String, optional): UPLOADED, APPROVED, REJECTED, DUPLICATE
  - from (LocalDateTime, optional)
  - to (LocalDateTime, optional)
  - page (int, default: 0)
  - size (int, default: 20)
Response: Page<ReceiptClaimResponse>
```

#### List Pending Claims
```
GET /api/admin/receipt-claims/pending?restaurantId=optional&page=0&size=20
Response: Page<ReceiptClaimResponse>
```

#### Get Receipt Claim Details
```
GET /api/admin/receipt-claims/{claimId}
Response: ReceiptClaimResponse
```

#### Get Receipt Claim Full Details
```
GET /api/admin/receipt-claims/{claimId}/details
Response: ReceiptClaimResponse (with all metadata)
```

#### Approve Receipt Claim
```
PUT /api/admin/receipt-claims/{claimId}/approve
Body: ApproveReceiptRequest {
  pointsOverride (required),
  notes (optional)
}
Response: ReceiptClaimResponse (status=APPROVED)
```

#### Reject Receipt Claim
```
PUT /api/admin/receipt-claims/{claimId}/reject
Body: RejectReceiptRequest {
  reason (required),
  notes (optional)
}
Response: ReceiptClaimResponse (status=REJECTED)
```

#### Bulk Approve Claims
```
POST /api/admin/receipt-claims/approve-bulk
Body: {
  "claimIds": [1, 2, 3],
  "pointsOverride": 20 (optional, null = use calculated)
}
Response: { "totalRequested": 3, "approved": 3 }
```

#### Bulk Reject Claims
```
POST /api/admin/receipt-claims/reject-bulk
Body: {
  "claimIds": [1, 2, 3],
  "reason": "Invalid receipt format"
}
Response: { "totalRequested": 3, "rejected": 3 }
```

#### Get Claims by User
```
GET /api/admin/receipt-claims/by-user/{userId}?page=0&size=20
Response: Page<ReceiptClaimResponse>
```

#### Get Claims by Restaurant
```
GET /api/admin/receipt-claims/by-restaurant/{restaurantId}?status=optional&page=0&size=20
Response: Page<ReceiptClaimResponse>
```

#### Get Duplicate Claims
```
GET /api/admin/receipt-claims/duplicates?restaurantId=optional&userId=optional&page=0&size=20
Response: Page<ReceiptClaimResponse>
```

#### Export Receipt Claims to CSV
```
GET /api/admin/receipt-claims/export?restaurantId=optional&status=optional&from=optional&to=optional
Response: CSV file (attachment)
```

#### Get Receipt Claims Statistics
```
GET /api/admin/receipt-claims/statistics?restaurantId=optional&from=optional&to=optional
Response: {
  "totalClaims": 500,
  "approved": 450,
  "rejected": 30,
  "pending": 15,
  "duplicate": 5,
  "totalPointsClaimed": 7500,
  "averagePointsPerClaim": 15
}
```

#### Mark as Duplicate
```
POST /api/admin/receipt-claims/{claimId}/mark-duplicate?duplicateOfId=optional
Response: ReceiptClaimResponse (status=DUPLICATE)
```

#### Revert Claim to Pending
```
POST /api/admin/receipt-claims/{claimId}/revert
Response: ReceiptClaimResponse (status=UPLOADED)
```

---

## Admin Points Management API Endpoints

### Base URL: `/api/admin/points`

#### Credit Points to User
```
POST /api/admin/points/credit
Body: CreditPointsRequest {
  userId (required),
  amount (required),
  reason (optional)
}
Response: { "userId": 123, "pointsAdded": 50, "newBalance": 300 }
```

#### Debit Points from User
```
POST /api/admin/points/debit
Body: {
  "userId": 123,
  "amount": 50,
  "reason": "Adjustment"
}
Response: { "userId": 123, "pointsDeducted": 50, "newBalance": 250 }
```

#### Bulk Credit Points
```
POST /api/admin/points/bulk-credit
Body: BulkCreditRequest {
  userIds: [1, 2, 3],
  amount: 50,
  reason: "Promotion"
}
Response: {
  "totalUsers": 3,
  "pointsPerUser": 50,
  "totalPointsDistributed": 150
}
```

#### Reverse Points
```
POST /api/admin/points/reverse
Body: ReversePointsRequest {
  userId,
  amount,
  reason
}
Response: { "userId": 123, "pointsReversed": 50, "newBalance": 250 }
```

#### Get User Points Balance
```
GET /api/admin/points/balance/{userId}
Response: { "userId": 123, "balance": 300 }
```

#### Get User Points Ledger
```
GET /api/admin/points/ledger/{userId}?page=0&size=20
Response: Page<PointsLedgerEntry>
```

#### Set User Points Balance
```
POST /api/admin/points/set-balance
Body: {
  "userId": 123,
  "newBalance": 500,
  "reason": "Manual adjustment"
}
Response: { "userId": 123, "newBalance": 500 }
```

#### Get Points Statistics
```
GET /api/admin/points/statistics?restaurantId=optional
Response: {
  "totalPointsDistributed": 500000,
  "totalPointsRedeemed": 450000,
  "averagePointsPerUser": 1500,
  "usersWithPoints": 300
}
```

#### Get Top Earners
```
GET /api/admin/points/top-earners?restaurantId=optional&limit=10
Response: [
  { "userId": 1, "userName": "John", "points": 5000 },
  ...
]
```

#### Bulk Reverse Points
```
POST /api/admin/points/bulk-reverse
Body: {
  "reverses": [
    { "userId": 1, "amount": 50 },
    { "userId": 2, "amount": 100 }
  ]
}
Response: { "totalReverses": 2, "completedReverses": 2 }
```

---

## Admin Rewards Management API Endpoints

### Base URL: `/api/admin/rewards`

#### List Reward Tiers
```
GET /api/admin/rewards/tiers?page=0&size=20
Response: Page<RewardTierResponse>
```

#### Get Reward Tier Details
```
GET /api/admin/rewards/tiers/{tierId}
Response: RewardTierResponse
```

#### Create Reward Tier
```
POST /api/admin/rewards/tiers
Body: RewardTierRequest {
  name, pointsThreshold, tierOrder,
  restaurantId, perks, color
}
Response: RewardTierResponse
```

#### Update Reward Tier
```
PUT /api/admin/rewards/tiers/{tierId}
Body: RewardTierRequest
Response: RewardTierResponse
```

#### Delete Reward Tier
```
DELETE /api/admin/rewards/tiers/{tierId}
Response: Success message
```

#### Duplicate Reward Tier
```
PUT /api/admin/rewards/tiers/{tierId}/duplicate?newName=optional
Response: RewardTierResponse
```

#### Get Ways to Earn
```
GET /api/admin/rewards/ways-to-earn
Response: List<WayToEarnRequest>
```

#### Create Way to Earn
```
POST /api/admin/rewards/ways-to-earn
Body: WayToEarnRequest
Response: WayToEarnRequest
```

#### Update Way to Earn
```
PUT /api/admin/rewards/ways-to-earn/{ruleId}
Body: WayToEarnRequest
Response: WayToEarnRequest
```

#### Delete Way to Earn
```
DELETE /api/admin/rewards/ways-to-earn/{ruleId}
Response: Success message
```

#### Get Reward Settings
```
GET /api/admin/rewards/settings
Response: RewardSettingsRequest
```

#### Update Reward Settings
```
PUT /api/admin/rewards/settings
Body: RewardSettingsRequest {
  pointsPerPurchase, maxPointsPerDay, etc.
}
Response: RewardSettingsRequest
```

#### Get Rewards Statistics
```
GET /api/admin/rewards/statistics?restaurantId=optional
Response: {
  "totalMembers": 500,
  "totalTiers": 3,
  "totalPointsDistributed": 500000,
  "averagePointsPerMember": 1000
}
```

#### Get User Tier Distribution
```
GET /api/admin/rewards/user-tier-distribution?restaurantId=optional
Response: {
  "silver": 200,
  "gold": 150,
  "platinum": 50
}
```

---

## Authentication & Security

All admin endpoints require:
- **Authentication**: Valid JWT token in `Authorization: Bearer <token>` header
- **Authorization**: `ROLE_ADMIN` or equivalent admin role
- **Rate Limiting**: 100 requests per minute per admin user

---

## Error Responses

All endpoints return standardized error responses:

```json
{
  "success": false,
  "message": "Error description",
  "error": "ERROR_CODE",
  "timestamp": "2026-09-16T10:30:00Z"
}
```

Common HTTP Status Codes:
- `200 OK` - Success
- `201 Created` - Resource created
- `400 Bad Request` - Invalid request
- `401 Unauthorized` - Missing/invalid auth
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `409 Conflict` - Business logic error
- `500 Internal Server Error` - Server error

---

## Notes

- All admin endpoints require ADMIN role
- Timestamps use ISO 8601 format: `2026-09-16T10:30:00Z`
- Pagination defaults: page=0, size=20 (max 100)
- Bulk operations limit: 1000 items per request
- CSV exports include all related data
- All operations are logged for audit purposes

