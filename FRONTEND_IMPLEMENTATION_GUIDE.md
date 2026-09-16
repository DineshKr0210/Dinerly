# Dinerly Frontend Implementation Guide
## Offers, Rewards, and Points API Integration

---

## Table of Contents
1. [Overview](#overview)
2. [Authentication Flow](#authentication-flow)
3. [Offers Implementation](#offers-implementation)
4. [Rewards Implementation](#rewards-implementation)
5. [Points Implementation](#points-implementation)
6. [Complete Integration Workflow](#complete-integration-workflow)

---

## Overview

The Dinerly backend provides comprehensive APIs for managing:
- **Offers**: Discount coupons and promotional codes
- **Rewards**: Tiered loyalty program with redemption items
- **Points**: Guest loyalty points that earn and can be redeemed

### Base URL
```
Development: http://localhost:8080
Staging: https://staging.dinerly.com
Production: https://api.dinerly.com
```

### Authentication
All endpoints require JWT Bearer token in header:
```
Authorization: Bearer <JWT_TOKEN>
```

---

## Authentication Flow

### 1. Guest Login
**Endpoint**: `POST /api/auth/login`
```json
Request:
{
  "email": "guest@example.com",
  "password": "password123"
}

Response:
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 123,
      "email": "guest@example.com",
      "role": "GUEST",
      "restaurantId": 1
    }
  }
}
```

### 2. Admin Login
**Endpoint**: `POST /api/auth/login`
```json
Request:
{
  "email": "admin@example.com",
  "password": "adminpass"
}

Response:
{
  "success": true,
  "data": {
    "token": "...",
    "user": {
      "id": 456,
      "email": "admin@example.com",
      "role": "ADMIN",
      "restaurantId": 1
    }
  }
}
```

---

## Offers Implementation

### Admin: Create Offers

#### Step 1: Create a New Offer
**Endpoint**: `POST /api/admin/offers`
**Role**: ADMIN
```json
Request Headers:
{
  "Authorization": "Bearer <ADMIN_TOKEN>",
  "Content-Type": "application/json"
}

Request Body:
{
  "name": "20% Off Dinner",
  "locationId": 1,
  "startDate": "2026-09-16",
  "endDate": "2026-10-16",
  "status": "ACTIVE",
  "discountValue": 20.00,
  "discountLabel": "20% off",
  "description": "Get 20% discount on your entire meal",
  "photoUrl": "https://example.com/offer-image.jpg",
  "perUserLimit": 5
}

Response:
{
  "success": true,
  "message": "Offer created successfully",
  "data": {
    "id": 789,
    "name": "20% Off Dinner",
    "locationId": 1,
    "locationName": "Downtown Restaurant",
    "startDate": "2026-09-16",
    "endDate": "2026-10-16",
    "status": "ACTIVE",
    "discountValue": 20.00,
    "discountLabel": "20% off",
    "description": "Get 20% discount on your entire meal",
    "photoUrl": "https://example.com/offer-image.jpg",
    "perUserLimit": 5,
    "redemptions": 0,
    "createdAt": "2026-09-16T10:00:00",
    "updatedAt": "2026-09-16T10:00:00"
  }
}
```

#### Step 2: Update an Offer
**Endpoint**: `PUT /api/admin/offers/{offerId}`
**Role**: ADMIN
```json
Request:
{
  "name": "25% Off Dinner",
  "locationId": 1,
  "startDate": "2026-09-16",
  "endDate": "2026-11-16",
  "status": "ACTIVE",
  "discountValue": 25.00,
  "discountLabel": "25% off",
  "description": "Updated: Get 25% discount on your entire meal",
  "photoUrl": "https://example.com/offer-image-new.jpg",
  "perUserLimit": 10
}
```

#### Step 3: List All Offers (Admin View)
**Endpoint**: `GET /api/admin/offers`
**Role**: ADMIN
**Query Parameters**:
- `locationId` (optional): Filter by location
- `status` (optional): Filter by status (ACTIVE, INACTIVE, DRAFT)
- `category` (optional): Filter by category
- `page` (default: 0): Pagination page
- `size` (default: 20): Items per page

```bash
curl -X GET "http://localhost:8080/api/admin/offers?locationId=1&status=ACTIVE&page=0&size=20" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

#### Step 4: Get Single Offer Details
**Endpoint**: `GET /api/admin/offers/{offerId}`
**Role**: ADMIN
```bash
curl -X GET "http://localhost:8080/api/admin/offers/789" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

#### Step 5: Toggle Offer Status
**Endpoint**: `PUT /api/admin/offers/{offerId}/toggle-status`
**Role**: ADMIN
```bash
curl -X PUT "http://localhost:8080/api/admin/offers/789/toggle-status" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

#### Step 6: Duplicate an Offer
**Endpoint**: `POST /api/admin/offers/{offerId}/duplicate`
**Role**: ADMIN
```bash
curl -X POST "http://localhost:8080/api/admin/offers/789/duplicate?newName=Copy%20of%20Offer" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

#### Step 7: Delete an Offer
**Endpoint**: `DELETE /api/admin/offers/{offerId}`
**Role**: ADMIN
```bash
curl -X DELETE "http://localhost:8080/api/admin/offers/789" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

---

### Guest: Browse and Redeem Offers

#### Step 1: List Available Offers
**Endpoint**: `GET /api/offers`
**Role**: GUEST or Public
```bash
curl -X GET "http://localhost:8080/api/offers?locationId=1&page=0&size=20"
```

Response:
```json
{
  "success": true,
  "message": "Offers retrieved successfully",
  "data": {
    "content": [
      {
        "id": 789,
        "name": "20% Off Dinner",
        "restaurantId": 1,
        "restaurantName": "Downtown Restaurant",
        "startDate": "2026-09-16",
        "endDate": "2026-10-16",
        "status": "ACTIVE",
        "discountValue": 20.00,
        "discountLabel": "20% off",
        "description": "Get 20% discount on your entire meal",
        "photoUrl": "https://example.com/offer-image.jpg",
        "redeemable": true,
        "reasonIfNotRedeemable": null,
        "userRedemptionsTotal": 2
      }
    ],
    "totalElements": 50,
    "totalPages": 3,
    "currentPage": 0
  }
}
```

#### Step 2: Get Offer Details
**Endpoint**: `GET /api/offers/{offerId}`
**Role**: GUEST or Public
```bash
curl -X GET "http://localhost:8080/api/offers/789" \
  -H "Authorization: Bearer <GUEST_TOKEN>"
```

#### Step 3: Redeem an Offer (Generate Code)
**Endpoint**: `POST /api/offers/{offerId}/redeem`
**Role**: GUEST (Authenticated)
```bash
curl -X POST "http://localhost:8080/api/offers/789/redeem?locationId=1" \
  -H "Authorization: Bearer <GUEST_TOKEN>"
```

Response:
```json
{
  "success": true,
  "message": "Offer redeemed successfully",
  "data": {
    "redemptionId": 456,
    "redemptionCode": "DINE-XYZ123-ABC456",
    "status": "GENERATED",
    "offerName": "20% Off Dinner",
    "offerDescription": "Get 20% discount on your entire meal",
    "userId": 123,
    "userEmail": "guest@example.com",
    "offerId": 789,
    "discountType": "FIXED",
    "discountValue": 20.00,
    "discountLabel": "20% off",
    "generatedAt": "2026-09-16T14:30:00",
    "codeExpiresAt": "2026-09-16T15:30:00"
  }
}
```

#### Step 4: Confirm/Validate Redemption Code (Staff/Admin at Register)
**Endpoint**: `POST /api/offers/redeem/{code}/confirm`
**Role**: STAFF or ADMIN
**Note**: This is called when staff scans the code at the restaurant

```bash
curl -X POST "http://localhost:8080/api/offers/redeem/DINE-XYZ123-ABC456/confirm" \
  -H "Authorization: Bearer <STAFF_TOKEN>"
```

Response:
```json
{
  "success": true,
  "message": "Code validated and redeemed",
  "data": {
    "redemptionId": 456,
    "redemptionCode": "DINE-XYZ123-ABC456",
    "status": "REDEEMED",
    "offerName": "20% Off Dinner",
    "offerDescription": "Get 20% discount on your entire meal",
    "userId": 123,
    "userEmail": "guest@example.com",
    "mobileNumber": "+1-234-567-8900",
    "offerId": 789,
    "discountType": "FIXED",
    "discountValue": 20.00,
    "discountLabel": "20% off",
    "confirmedAt": "2026-09-16T14:35:00",
    "codeExpiresAt": "2026-09-16T15:30:00"
  }
}
```

---

## Rewards Implementation

### Admin: Setup Rewards Program

#### Step 1: Create Reward Tiers
**Endpoint**: `POST /api/admin/rewards/tiers`
**Role**: ADMIN
```json
Request:
{
  "name": "Gold Tier",
  "minPoints": 1000,
  "maxPoints": 4999,
  "benefits": ["Free appetizer", "10% discount"],
  "icon": "gold-badge.png"
}

Response:
{
  "success": true,
  "message": "Tier created successfully",
  "data": {
    "id": 101,
    "name": "Gold Tier",
    "minPoints": 1000,
    "maxPoints": 4999,
    "benefits": ["Free appetizer", "10% discount"],
    "icon": "gold-badge.png",
    "createdAt": "2026-09-16T10:00:00"
  }
}
```

#### Step 2: List All Reward Tiers
**Endpoint**: `GET /api/admin/rewards/tiers`
**Role**: ADMIN
```bash
curl -X GET "http://localhost:8080/api/admin/rewards/tiers?page=0&size=20" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

#### Step 3: Create Reward Items (Rewards to Redeem)
**Endpoint**: `POST /api/admin/reward-items`
**Role**: ADMIN
```json
Request:
{
  "name": "Free Appetizer",
  "pointsRequired": 500,
  "description": "Redeem for a free appetizer from our menu",
  "restaurantId": 1,
  "category": "Food",
  "image": "appetizer.jpg",
  "available": true,
  "inventory": 100
}

Response:
{
  "success": true,
  "message": "Reward item created successfully",
  "data": {
    "id": 201,
    "name": "Free Appetizer",
    "pointsRequired": 500,
    "description": "Redeem for a free appetizer from our menu",
    "restaurantId": 1,
    "category": "Food",
    "image": "appetizer.jpg",
    "available": true,
    "inventory": 100,
    "createdAt": "2026-09-16T10:00:00"
  }
}
```

#### Step 4: Configure Ways to Earn Points
**Endpoint**: `POST /api/admin/rewards/ways-to-earn`
**Role**: ADMIN
```json
Request:
{
  "action": "VISIT",
  "pointsValue": 10,
  "description": "Earn 10 points per restaurant visit"
}

Response:
{
  "success": true,
  "message": "Way to earn created",
  "data": {
    "id": 301,
    "action": "VISIT",
    "pointsValue": 10,
    "description": "Earn 10 points per restaurant visit"
  }
}
```

**Common Actions**:
- `VISIT`: Points earned per visit
- `PURCHASE`: Points earned per dollar spent
- `REFERRAL`: Points earned for referrals
- `SIGN_UP`: Bonus points for signing up
- `BIRTHDAY`: Bonus points on birthday

#### Step 5: Configure Reward Settings
**Endpoint**: `PUT /api/admin/rewards/settings`
**Role**: ADMIN
```json
Request:
{
  "programName": "Dinerly Loyalty",
  "pointsPerDollar": 1,
  "minPointsToRedeem": 100,
  "pointsExpiryDays": 365
}

Response:
{
  "success": true,
  "message": "Settings updated successfully",
  "data": { ... }
}
```

#### Step 6: Get Reward Statistics
**Endpoint**: `GET /api/admin/rewards/statistics`
**Role**: ADMIN
```bash
curl -X GET "http://localhost:8080/api/admin/rewards/statistics?restaurantId=1" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

---

### Guest: View and Redeem Rewards

#### Step 1: Get Rewards Profile
**Endpoint**: `GET /api/rewards/profile`
**Role**: GUEST (Authenticated)
```bash
curl -X GET "http://localhost:8080/api/rewards/profile?restaurantId=1" \
  -H "Authorization: Bearer <GUEST_TOKEN>"
```

Response:
```json
{
  "success": true,
  "message": "Rewards profile retrieved successfully",
  "data": {
    "userId": 123,
    "currentPoints": 1250,
    "currentTier": {
      "id": 101,
      "name": "Gold Tier",
      "minPoints": 1000,
      "maxPoints": 4999,
      "benefits": ["Free appetizer", "10% discount"]
    },
    "nextTier": {
      "id": 102,
      "name": "Platinum Tier",
      "minPoints": 5000,
      "pointsUntilNextTier": 3750
    },
    "availableRewards": [
      {
        "id": 201,
        "name": "Free Appetizer",
        "pointsRequired": 500,
        "description": "Redeem for a free appetizer",
        "canRedeem": true
      },
      {
        "id": 202,
        "name": "Free Entree",
        "pointsRequired": 1000,
        "description": "Redeem for a free entree",
        "canRedeem": true
      }
    ],
    "pointsEarningRate": 1,
    "memberSince": "2025-01-15"
  }
}
```

#### Step 2: Redeem a Reward Item
**Endpoint**: `POST /api/rewards/{rewardId}/redeem`
**Role**: GUEST (Authenticated)
```json
Request:
{
  "restaurantId": 1
}

Response:
{
  "success": true,
  "message": "Reward redeemed successfully",
  "data": {
    "redemptionId": 501,
    "rewardId": 201,
    "rewardName": "Free Appetizer",
    "pointsDeducted": 500,
    "newBalance": 750,
    "redemptionCode": "REWARD-ABC123-XYZ789",
    "expiresAt": "2026-09-23T14:30:00",
    "status": "PENDING_USE"
  }
}
```

#### Step 3: Claim Receipt for Points
**Endpoint**: `POST /api/rewards/receipt/claim`
**Role**: GUEST (Authenticated)
```bash
curl -X POST "http://localhost:8080/api/rewards/receipt/claim" \
  -F "file=@receipt.jpg" \
  -F "restaurantId=1" \
  -F "receiptAmount=50.00" \
  -F "receiptDate=2026-09-16" \
  -H "Authorization: Bearer <GUEST_TOKEN>"
```

Response:
```json
{
  "success": true,
  "message": "Receipt claimed successfully",
  "data": {
    "claimId": 601,
    "pointsAwarded": 50,
    "newBalance": 800,
    "status": "PENDING_REVIEW",
    "message": "Receipt submitted for verification"
  }
}
```

---

## Points Implementation

### Admin: Manage Points

#### Step 1: Credit Points to Guest
**Endpoint**: `POST /api/admin/points/credit`
**Role**: ADMIN
```json
Request:
{
  "userId": 123,
  "amount": 100,
  "reason": "Promotion: Birthday Bonus"
}

Response:
{
  "success": true,
  "message": "Points credited successfully",
  "data": {
    "userId": 123,
    "pointsAdded": 100,
    "newBalance": 1350
  }
}
```

#### Step 2: Debit Points from Guest
**Endpoint**: `POST /api/admin/points/debit`
**Role**: ADMIN
```json
Request:
{
  "userId": 123,
  "amount": 50,
  "reason": "Correction: Duplicate entry"
}

Response:
{
  "success": true,
  "message": "Points debited successfully",
  "data": {
    "userId": 123,
    "pointsDeducted": 50,
    "newBalance": 1300
  }
}
```

#### Step 3: Bulk Credit Points
**Endpoint**: `POST /api/admin/points/bulk-credit`
**Role**: ADMIN
```json
Request:
{
  "userIds": [123, 124, 125],
  "amount": 50,
  "reason": "Weekly Promotion"
}

Response:
{
  "success": true,
  "message": "Bulk credit completed successfully",
  "data": {
    "totalUsers": 3,
    "pointsPerUser": 50,
    "totalPointsDistributed": 150
  }
}
```

#### Step 4: Get User Points Balance
**Endpoint**: `GET /api/admin/points/balance/{userId}`
**Role**: ADMIN
```bash
curl -X GET "http://localhost:8080/api/admin/points/balance/123" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

Response:
```json
{
  "success": true,
  "message": "User points retrieved successfully",
  "data": {
    "userId": 123,
    "balance": 1300
  }
}
```

#### Step 5: Reverse Points
**Endpoint**: `POST /api/admin/points/reverse`
**Role**: ADMIN
```json
Request:
{
  "userId": 123,
  "amount": 25,
  "reason": "Adjustment for returned item"
}

Response:
{
  "success": true,
  "message": "Points reversed successfully",
  "data": {
    "userId": 123,
    "pointsReversed": 25,
    "newBalance": 1275
  }
}
```

#### Step 6: Get Points Ledger/History
**Endpoint**: `GET /api/admin/points/ledger/{userId}`
**Role**: ADMIN
**Query Parameters**:
- `page` (default: 0)
- `size` (default: 20)

```bash
curl -X GET "http://localhost:8080/api/admin/points/ledger/123?page=0&size=20" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

#### Step 7: Get Points Statistics
**Endpoint**: `GET /api/admin/points/statistics`
**Role**: ADMIN
```bash
curl -X GET "http://localhost:8080/api/admin/points/statistics?restaurantId=1" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

#### Step 8: View Top Earners
**Endpoint**: `GET /api/admin/points/top-earners`
**Role**: ADMIN
```bash
curl -X GET "http://localhost:8080/api/admin/points/top-earners?restaurantId=1&limit=10" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

---

## Complete Integration Workflow

### Scenario: Full Customer Journey

#### 1. Customer Signs Up
```
POST /api/auth/register
→ Returns JWT token
→ Guest account created
→ Points account initialized with 0 balance
```

#### 2. Admin Sets Up Loyalty Program
```
POST /api/admin/rewards/tiers          (Create Bronze, Silver, Gold tiers)
POST /api/admin/reward-items           (Add reward items: Free Appetizer, Drink, Dessert)
POST /api/admin/rewards/ways-to-earn   (Configure earning rules)
PUT /api/admin/rewards/settings        (Configure program settings)
```

#### 3. Admin Creates Promotional Offers
```
POST /api/admin/offers                 (Create "20% Off Dinner" offer)
POST /api/admin/offers                 (Create "Free Dessert" offer)
```

#### 4. Guest Browses Offers
```
GET /api/offers?locationId=1           (Browse available offers)
GET /api/offers/789                    (View offer details)
```

#### 5. Guest Redeems Offer
```
POST /api/offers/789/redeem            (Generate redemption code)
→ Returns: DINE-XYZ123-ABC456
```

#### 6. Guest Uses Code at Restaurant
```
Staff/Admin: POST /api/offers/redeem/DINE-XYZ123-ABC456/confirm
→ Code validated and marked as redeemed
→ Guest receives discount
```

#### 7. Admin Credits Points for Purchase
```
POST /api/admin/points/credit          (Award 50 points for $50 purchase)
→ Guest balance: 50 points
```

#### 8. Guest Views Rewards Profile
```
GET /api/rewards/profile?restaurantId=1
→ Current points: 50
→ Available rewards with point requirements
→ Current tier status
```

#### 9. Guest Earns More Points
```
Multiple visits and purchases:
POST /api/admin/points/credit          (10 points per visit)
→ Guest balance: 150 points
→ Now eligible for "Free Appetizer" (500 pts needed)
```

#### 10. Guest Redeems Reward
```
POST /api/rewards/201/redeem           (Redeem Free Appetizer)
→ 500 points deducted
→ Guest balance: -350? NO! (needs 500+ points)
→ Error: "Insufficient points"

After more visits/purchases:
→ Guest balance: 750 points
POST /api/rewards/201/redeem           (Redeem Free Appetizer)
→ 500 points deducted
→ Guest balance: 250 points
→ Returns: REWARD-ABC123-XYZ789 (Redemption code)
```

#### 11. Guest Uses Reward at Restaurant
```
Staff: Scans REWARD-ABC123-XYZ789
→ Validates reward
→ Applies free appetizer discount
```

---

## Error Handling

### Common Error Responses

#### 401 Unauthorized
```json
{
  "success": false,
  "message": "Unauthorized: Invalid or expired token",
  "data": null
}
```

#### 403 Forbidden
```json
{
  "success": false,
  "message": "Forbidden: Insufficient permissions",
  "data": null
}
```

#### 400 Bad Request - Insufficient Points
```json
{
  "success": false,
  "message": "Cannot redeem: Insufficient points (Have: 250, Required: 500)",
  "data": null
}
```

#### 400 Bad Request - Offer Expired
```json
{
  "success": false,
  "message": "Offer has expired and is no longer available",
  "data": null
}
```

#### 404 Not Found
```json
{
  "success": false,
  "message": "Offer not found with ID: 999",
  "data": null
}
```

#### 422 Unprocessable Entity - Validation Error
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "errors": [
      {
        "field": "discountValue",
        "message": "Discount value must be greater than 0"
      }
    ]
  }
}
```

---

## Best Practices for Frontend

### 1. Token Management
- Store JWT token securely (httpOnly cookies recommended)
- Implement token refresh mechanism (check expiry)
- Clear token on logout

### 2. Error Handling
- Always check `success` flag in response
- Display user-friendly error messages
- Log errors for debugging
- Retry failed requests with exponential backoff

### 3. Loading States
- Show loading indicator during API calls
- Disable buttons during submission
- Show skeleton screens for paginated lists

### 4. Pagination
- Store pagination state in component
- Implement infinite scroll or "Load More"
- Cache previously loaded pages

### 5. Real-Time Updates
- Refresh points balance after redemption
- Update rewards profile after earning points
- Show live offer availability

### 6. Validation
- Validate form inputs before sending
- Handle backend validation errors gracefully
- Show field-specific error messages

### 7. Caching Strategy
```javascript
// Cache offers list for 5 minutes
const offersCacheKey = 'offers_list_page_0';
const cacheExpiry = 5 * 60 * 1000; // 5 minutes

// Cache user profile (refresh on logout)
const userProfileCacheKey = 'user_profile';

// Cache reward tiers (refresh on settings update)
const rewardsTiersCacheKey = 'rewards_tiers';
```

---

## Testing Checklist

### Offers
- [ ] Admin creates offer with all 10 fields
- [ ] Guest lists offers and filters by location
- [ ] Guest redeems offer and gets code
- [ ] Staff validates code at register
- [ ] Code expires after 1 hour
- [ ] Offer respects per-user limit
- [ ] Admin can toggle offer status
- [ ] Admin can delete/archive offers

### Rewards
- [ ] Admin creates tiers with benefits
- [ ] Admin adds reward items
- [ ] Guest views rewards profile
- [ ] Guest sees eligible vs locked rewards
- [ ] Guest redeems reward with code
- [ ] Points deducted correctly
- [ ] Tier upgrades when points increase
- [ ] Receipt upload awards points

### Points
- [ ] Admin credits points to guest
- [ ] Admin debits points (adjustment)
- [ ] Bulk operations work correctly
- [ ] Points balance updates immediately
- [ ] Ledger shows all transactions
- [ ] Points don't go negative
- [ ] Expiry rules enforced (if configured)

---

## Rate Limits (Recommended)

```
/api/auth/login              : 5 requests per minute
/api/offers/*                : 60 requests per minute
/api/admin/offers/*          : 30 requests per minute
/api/rewards/*               : 60 requests per minute
/api/admin/rewards/*         : 30 requests per minute
/api/admin/points/*          : 30 requests per minute
Default                      : 100 requests per minute
```

---

## Support & Documentation

- **API Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI Spec**: `http://localhost:8080/v3/api-docs`
- **Backend Repository**: Check README.md for deployment info
- **Contact**: support@dinerly.com

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2026-09-16 | Initial guide with Offers, Rewards, and Points implementation |

---

**Last Updated**: 2026-09-16
**Status**: Production Ready ✅
