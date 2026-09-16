# 🧪 Complete E2E Testing Guide - Admin to Guest Flow
**Offers, Rewards, Points & Redemption Testing**

**Date:** 2026-09-16  
**API Version:** 1.0  
**Base URL:** `http://localhost:8080`  
**Authentication:** JWT Bearer Token in `Authorization` header

---

## 📋 Table of Contents

1. [Setup & Prerequisites](#setup--prerequisites)
2. [Admin: Create Offers](#admin-create-offers)
3. [Admin: Create Rewards Program](#admin-create-rewards-program)
4. [Admin: Configure Points Earning](#admin-configure-points-earning)
5. [Guest: Browse & Redeem Offers](#guest-browse--redeem-offers)
6. [Guest: View Rewards & Claim Receipt](#guest-view-rewards--claim-receipt)
7. [Admin: Manage Redemptions](#admin-manage-redemptions)
8. [Admin: Approve Receipt Claims](#admin-approve-receipt-claims)
9. [Admin: View Analytics & Reports](#admin-view-analytics--reports)

---

## Setup & Prerequisites

### Required Headers for All Requests
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer YOUR_JWT_TOKEN_HERE"
}
```

### Test User Accounts
```
Admin User:
  - Email: admin@restaurant.com
  - Password: Admin@2026
  - Role: ROLE_ADMIN

Guest User:
  - Email: guest@example.com
  - Password: Guest@2026
  - Role: ROLE_USER

Restaurant ID: 1
Location ID: 1
```

### Get Authentication Token
```bash
POST /api/auth/login
Content-Type: application/json

{
  "email": "admin@restaurant.com",
  "password": "Admin@2026"
}

RESPONSE (200):
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1,
    "email": "admin@restaurant.com",
    "roles": ["ROLE_ADMIN"]
  }
}
```

---

# 🎯 ADMIN FLOW - Setup Phase

---

## STEP 1: Admin Creates Offers

### 1.1 Create First Offer (PERCENT Discount)

```bash
POST /api/admin/offers
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "restaurantId": 1,
  "name": "Happy Hour - 20% Off",
  "description": "20% discount on all food items during happy hour (4-6 PM)",
  "startDate": "2026-09-16",
  "endDate": "2026-12-31",
  "status": "ACTIVE",
  "discountType": "PERCENT",
  "discountValue": 20.0,
  "discountLabel": "20% Off All Items",
  "category": "PERCENT",
  "originalPrice": 100.0,
  "photoUrl": "https://example.com/happy-hour.jpg",
  "rating": 4.5,
  "ratingCount": 250L,
  "perUserLimit": 5,
  "perUserDailyLimit": 1,
  "inventory": 1000,
  "restrictions": ["Must purchase minimum $20", "Valid 4-6 PM only"]
}

RESPONSE (201):
{
  "success": true,
  "message": "Offer created successfully",
  "data": {
    "id": 1,
    "name": "Happy Hour - 20% Off",
    "discountType": "PERCENT",
    "discountValue": 20.0,
    "discountLabel": "20% Off All Items",
    "status": "ACTIVE",
    "startDate": "2026-09-16",
    "endDate": "2026-12-31",
    "inventory": 1000,
    "perUserLimit": 5,
    "perUserDailyLimit": 1,
    "createdAt": "2026-09-16T10:30:00Z",
    "updatedAt": "2026-09-16T10:30:00Z"
  }
}
```

**✅ SAVE:** `OFFER_ID_1 = 1`

### 1.2 Create Second Offer (FIXED Discount)

```bash
POST /api/admin/offers
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "restaurantId": 1,
  "name": "Tuesday Deal - $10 Off",
  "description": "$10 discount on any purchase over $50",
  "startDate": "2026-09-16",
  "endDate": "2026-12-31",
  "status": "ACTIVE",
  "discountType": "FIXED",
  "discountValue": 10.0,
  "discountLabel": "$10 Off",
  "category": "FIXED",
  "originalPrice": 50.0,
  "photoUrl": "https://example.com/tuesday-deal.jpg",
  "rating": 4.8,
  "ratingCount": 420L,
  "perUserLimit": 2,
  "perUserDailyLimit": 1,
  "inventory": 500,
  "restrictions": ["Minimum order $50", "Tuesday only"]
}

RESPONSE (201):
{
  "id": 2,
  "name": "Tuesday Deal - $10 Off",
  ...
}
```

**✅ SAVE:** `OFFER_ID_2 = 2`

### 1.3 Create Third Offer (FREE_ITEM - Rewards Eligible)

```bash
POST /api/admin/offers
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "restaurantId": 1,
  "name": "Free Dessert Coupon",
  "description": "Get a free dessert with any entree purchase",
  "startDate": "2026-09-16",
  "endDate": "2026-12-31",
  "status": "ACTIVE",
  "discountType": "FREE_ITEM",
  "discountValue": 0.0,
  "discountLabel": "Free Dessert",
  "category": "REWARDS_ELIGIBLE",
  "originalPrice": 8.99,
  "photoUrl": "https://example.com/free-dessert.jpg",
  "rating": 4.9,
  "ratingCount": 890L,
  "perUserLimit": 3,
  "perUserDailyLimit": 1,
  "inventory": 200,
  "restrictions": ["With entree purchase", "Dine-in only"]
}

RESPONSE (201):
{
  "id": 3,
  "name": "Free Dessert Coupon",
  ...
}
```

**✅ SAVE:** `OFFER_ID_3 = 3`

### 1.4 Verify Offers Created

```bash
GET /api/admin/offers?page=0&size=20
Authorization: Bearer {ADMIN_TOKEN}

RESPONSE (200):
{
  "success": true,
  "data": {
    "content": [
      { "id": 1, "name": "Happy Hour - 20% Off", "status": "ACTIVE", ... },
      { "id": 2, "name": "Tuesday Deal - $10 Off", "status": "ACTIVE", ... },
      { "id": 3, "name": "Free Dessert Coupon", "status": "ACTIVE", ... }
    ],
    "totalElements": 3,
    "totalPages": 1
  }
}
```

---

## STEP 2: Admin Creates Reward Tiers

### 2.1 Create Silver Tier

```bash
POST /api/admin/rewards/tiers
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "restaurantId": 1,
  "name": "Silver Member",
  "pointsThreshold": 0,
  "tierOrder": 1,
  "color": "silver",
  "perks": [
    "2x points on birthday",
    "Birthday discount",
    "Early access to new offers"
  ]
}

RESPONSE (201):
{
  "id": 1,
  "name": "Silver Member",
  "pointsThreshold": 0,
  "tierOrder": 1,
  "color": "silver",
  "perks": [...],
  "createdAt": "2026-09-16T10:35:00Z"
}
```

**✅ SAVE:** `TIER_ID_1 = 1`

### 2.2 Create Gold Tier

```bash
POST /api/admin/rewards/tiers
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "restaurantId": 1,
  "name": "Gold Member",
  "pointsThreshold": 350,
  "tierOrder": 2,
  "color": "gold",
  "perks": [
    "3x points on purchases",
    "Free appetizer monthly",
    "Priority seating",
    "VIP events access"
  ]
}

RESPONSE (201):
{
  "id": 2,
  "name": "Gold Member",
  "pointsThreshold": 350,
  "tierOrder": 2,
  "color": "gold",
  ...
}
```

**✅ SAVE:** `TIER_ID_2 = 2`

### 2.3 Create Platinum Tier

```bash
POST /api/admin/rewards/tiers
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "restaurantId": 1,
  "name": "Platinum Member",
  "pointsThreshold": 700,
  "tierOrder": 3,
  "color": "platinum",
  "perks": [
    "5x points on all purchases",
    "Free meal voucher monthly",
    "Dedicated concierge",
    "Exclusive platinum events",
    "Lifetime warranty on reviews"
  ]
}

RESPONSE (201):
{
  "id": 3,
  "name": "Platinum Member",
  "pointsThreshold": 700,
  "tierOrder": 3,
  "color": "platinum",
  ...
}
```

**✅ SAVE:** `TIER_ID_3 = 3`

### 2.4 Verify Tiers Created

```bash
GET /api/admin/rewards/tiers?page=0&size=20
Authorization: Bearer {ADMIN_TOKEN}

RESPONSE (200):
{
  "data": {
    "content": [
      { "id": 1, "name": "Silver Member", "pointsThreshold": 0, ... },
      { "id": 2, "name": "Gold Member", "pointsThreshold": 350, ... },
      { "id": 3, "name": "Platinum Member", "pointsThreshold": 700, ... }
    ],
    "totalElements": 3
  }
}
```

---

## STEP 3: Admin Creates Reward Items

### 3.1 Create Beverage Reward

```bash
POST /api/admin/reward-items
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "restaurantId": 1,
  "title": "Free Coffee",
  "description": "Complimentary premium coffee of your choice",
  "pointsCost": 50,
  "category": "beverage",
  "icon": "☕",
  "available": true
}

RESPONSE (201):
{
  "id": 1,
  "title": "Free Coffee",
  "pointsCost": 50,
  "category": "beverage",
  "available": true,
  ...
}
```

**✅ SAVE:** `REWARD_ITEM_ID_1 = 1`

### 3.2 Create Food Reward

```bash
POST /api/admin/reward-items
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "restaurantId": 1,
  "title": "Free Appetizer",
  "description": "Choose any appetizer from the menu",
  "pointsCost": 100,
  "category": "food",
  "icon": "🍟",
  "available": true
}

RESPONSE (201):
{
  "id": 2,
  "title": "Free Appetizer",
  "pointsCost": 100,
  "category": "food",
  ...
}
```

**✅ SAVE:** `REWARD_ITEM_ID_2 = 2`

### 3.3 Create Dessert Reward

```bash
POST /api/admin/reward-items
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "restaurantId": 1,
  "title": "Free Dessert Platter",
  "description": "Deluxe dessert tasting platter for two",
  "pointsCost": 150,
  "category": "dessert",
  "icon": "🍰",
  "available": true
}

RESPONSE (201):
{
  "id": 3,
  "title": "Free Dessert Platter",
  "pointsCost": 150,
  "category": "dessert",
  ...
}
```

**✅ SAVE:** `REWARD_ITEM_ID_3 = 3`

---

## STEP 4: Admin Configures Points Earning Rules

### 4.1 Create "Dine In" Earning Rule

```bash
POST /api/admin/points/earning-rules
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "restaurantId": 1,
  "action": "dine_in",
  "pointsValue": 15,
  "description": "Earn 15 points for every dine-in visit",
  "icon": "🍽️",
  "clickable": true,
  "actionUrl": "/rewards/earn/dine-in"
}

RESPONSE (201):
{
  "id": 1,
  "action": "dine_in",
  "pointsValue": 15,
  ...
}
```

**✅ SAVE:** `RULE_ID_1 = 1`

### 4.2 Create "Join Waitlist" Earning Rule

```bash
POST /api/admin/points/earning-rules
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "restaurantId": 1,
  "action": "join_waitlist",
  "pointsValue": 10,
  "description": "Earn 10 points for joining our waitlist",
  "icon": "⏳",
  "clickable": true,
  "actionUrl": "/waitlist/join"
}

RESPONSE (201):
{
  "id": 2,
  "action": "join_waitlist",
  "pointsValue": 10,
  ...
}
```

**✅ SAVE:** `RULE_ID_2 = 2`

### 4.3 Create "Leave Review" Earning Rule

```bash
POST /api/admin/points/earning-rules
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "restaurantId": 1,
  "action": "leave_review",
  "pointsValue": 20,
  "description": "Earn 20 points for leaving a review",
  "icon": "⭐",
  "clickable": true,
  "actionUrl": "/reviews/new"
}

RESPONSE (201):
{
  "id": 3,
  "action": "leave_review",
  "pointsValue": 20,
  ...
}
```

**✅ SAVE:** `RULE_ID_3 = 3`

### 4.4 Verify Earning Rules

```bash
GET /api/admin/points/earning-rules?page=0&size=20
Authorization: Bearer {ADMIN_TOKEN}

RESPONSE (200):
{
  "data": {
    "content": [
      { "id": 1, "action": "dine_in", "pointsValue": 15, ... },
      { "id": 2, "action": "join_waitlist", "pointsValue": 10, ... },
      { "id": 3, "action": "leave_review", "pointsValue": 20, ... }
    ],
    "totalElements": 3
  }
}
```

---

# 👥 GUEST FLOW - Interaction Phase

---

## STEP 5: Guest Gets Authentication Token

```bash
POST /api/auth/login
Content-Type: application/json

{
  "email": "guest@example.com",
  "password": "Guest@2026"
}

RESPONSE (200):
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 2,
    "email": "guest@example.com",
    "roles": ["ROLE_USER"]
  }
}
```

**✅ SAVE:** `GUEST_TOKEN = eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`  
**✅ SAVE:** `GUEST_USER_ID = 2`

---

## STEP 6: Guest Browses Offers

### 6.1 Get All Offers

```bash
GET /api/offers?page=0&size=20
Authorization: Bearer {GUEST_TOKEN}

RESPONSE (200):
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Happy Hour - 20% Off",
        "discountLabel": "20% Off All Items",
        "description": "20% discount on all food items during happy hour (4-6 PM)",
        "status": "ACTIVE",
        "startDate": "2026-09-16",
        "endDate": "2026-12-31",
        "photoUrl": "https://example.com/happy-hour.jpg",
        "rating": 4.5,
        "ratingCount": 250,
        "currentPrice": 80.0,
        "originalPrice": 100.0,
        "redeemable": true,
        "reasonIfNotRedeemable": null,
        "userRedemptionsToday": 0,
        "userRedemptionsTotal": 0,
        "remainingInventory": 1000
      },
      {
        "id": 2,
        "name": "Tuesday Deal - $10 Off",
        "discountLabel": "$10 Off",
        ...
      },
      {
        "id": 3,
        "name": "Free Dessert Coupon",
        "discountLabel": "Free Dessert",
        ...
      }
    ],
    "totalElements": 3,
    "totalPages": 1
  }
}
```

### 6.2 Get Offer Details

```bash
GET /api/offers/1
Authorization: Bearer {GUEST_TOKEN}

RESPONSE (200):
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Happy Hour - 20% Off",
    "description": "20% discount on all food items during happy hour (4-6 PM)",
    "discountType": "PERCENT",
    "discountValue": 20.0,
    "discountLabel": "20% Off All Items",
    "status": "ACTIVE",
    "category": "PERCENT",
    "originalPrice": 100.0,
    "currentPrice": 80.0,
    "photoUrl": "https://example.com/happy-hour.jpg",
    "rating": 4.5,
    "ratingCount": 250,
    "restrictions": ["Must purchase minimum $20", "Valid 4-6 PM only"],
    "perUserLimit": 5,
    "perUserDailyLimit": 1,
    "inventory": 1000,
    "remainingInventory": 1000,
    "userRedemptionsToday": 0,
    "userRedemptionsTotal": 0,
    "redeemable": true,
    "reasonIfNotRedeemable": null,
    "expiresAt": "2026-12-31T23:59:59Z"
  }
}
```

---

## STEP 7: Guest Redeems Offer (Gets 6-Digit Code)

### 7.1 Redeem First Offer

```bash
POST /api/offers/1/redeem
Authorization: Bearer {GUEST_TOKEN}
Content-Type: application/json

{}

RESPONSE (201):
{
  "success": true,
  "message": "Offer redeemed successfully",
  "data": {
    "redemptionId": 101,
    "redemptionCode": "514527",
    "expiresAt": "2026-09-16T11:30:00Z",
    "message": "Your 6-digit code is ready! Show code at checkout",
    "offerName": "Happy Hour - 20% Off",
    "offerDescription": "20% discount on all food items during happy hour (4-6 PM)"
  }
}
```

**✅ SAVE:** `REDEMPTION_ID_1 = 101`  
**✅ SAVE:** `REDEMPTION_CODE_1 = "514527"`

### 7.2 Redeem Second Offer

```bash
POST /api/offers/2/redeem
Authorization: Bearer {GUEST_TOKEN}
Content-Type: application/json

{}

RESPONSE (201):
{
  "success": true,
  "data": {
    "redemptionId": 102,
    "redemptionCode": "823945",
    "expiresAt": "2026-09-16T11:30:00Z",
    "offerName": "Tuesday Deal - $10 Off",
    ...
  }
}
```

**✅ SAVE:** `REDEMPTION_ID_2 = 102`  
**✅ SAVE:** `REDEMPTION_CODE_2 = "823945"`

### 7.3 Verify Redemption Inventory Updated

```bash
GET /api/offers/1
Authorization: Bearer {GUEST_TOKEN}

RESPONSE (200):
{
  "data": {
    "id": 1,
    "remainingInventory": 999,  // Decreased from 1000
    "userRedemptionsToday": 1,
    "userRedemptionsTotal": 1
  }
}
```

---

## STEP 8: Staff Confirms Code at POS

### 8.1 Staff Confirms First Code

```bash
POST /api/offers/redeem/514527/confirm
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{}

RESPONSE (200):
{
  "success": true,
  "message": "Code validated and marked as completed",
  "data": {
    "redemptionId": 101,
    "status": "COMPLETED",
    "offerName": "Happy Hour - 20% Off",
    "userEmail": "guest@example.com",
    "discountValue": 20.0,
    "timestamp": "2026-09-16T10:45:00Z"
  }
}
```

### 8.2 Staff Confirms Second Code

```bash
POST /api/offers/redeem/823945/confirm
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{}

RESPONSE (200):
{
  "success": true,
  "message": "Code validated and marked as completed",
  "data": {
    "redemptionId": 102,
    "status": "COMPLETED",
    ...
  }
}
```

---

## STEP 9: Guest Views Rewards Profile

### 9.1 Get Complete Rewards Profile

```bash
GET /api/rewards/profile
Authorization: Bearer {GUEST_TOKEN}

RESPONSE (200):
{
  "success": true,
  "data": {
    "currentPoints": 45,
    "currentTier": {
      "id": 1,
      "name": "Silver Member",
      "color": "silver",
      "pointsThreshold": 0,
      "tierOrder": 1
    },
    "tierProgress": {
      "pointsToNextTier": 305,
      "nextTierName": "Gold Member",
      "progressPercentage": 12.86,
      "nextTierOrder": 2
    },
    "redeemableRewards": [
      {
        "id": 1,
        "title": "Free Coffee",
        "description": "Complimentary premium coffee of your choice",
        "pointsCost": 50,
        "icon": "☕",
        "status": "NOT_ENOUGH_POINTS"
      },
      {
        "id": 2,
        "title": "Free Appetizer",
        "description": "Choose any appetizer from the menu",
        "pointsCost": 100,
        "icon": "🍟",
        "status": "NOT_ENOUGH_POINTS"
      },
      {
        "id": 3,
        "title": "Free Dessert Platter",
        "description": "Deluxe dessert tasting platter for two",
        "pointsCost": 150,
        "icon": "🍰",
        "status": "NOT_ENOUGH_POINTS"
      }
    ],
    "waysToEarn": [
      {
        "action": "dine_in",
        "title": "Dine In",
        "subtitle": "Earn 15 points",
        "pointsValue": 15,
        "icon": "🍽️",
        "clickable": true,
        "actionUrl": "/rewards/earn/dine-in"
      },
      {
        "action": "join_waitlist",
        "title": "Join Waitlist",
        "subtitle": "Earn 10 points",
        "pointsValue": 10,
        "icon": "⏳",
        "clickable": true,
        "actionUrl": "/waitlist/join"
      },
      {
        "action": "leave_review",
        "title": "Leave Review",
        "subtitle": "Earn 20 points",
        "pointsValue": 20,
        "icon": "⭐",
        "clickable": true,
        "actionUrl": "/reviews/new"
      }
    ]
  }
}
```

**Note:** Guest has 45 points (likely from system initialization or previous interactions)

---

## STEP 10: Admin Credits Points to Guest

### 10.1 Credit 100 Points for Special Promotion

```bash
POST /api/admin/points/credit
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "userId": 2,
  "amount": 100,
  "reason": "Birthday promotion - 100 bonus points"
}

RESPONSE (200):
{
  "success": true,
  "message": "Points credited successfully",
  "data": {
    "userId": 2,
    "pointsAdded": 100,
    "newBalance": 145,
    "timestamp": "2026-09-16T11:00:00Z"
  }
}
```

### 10.2 Guest Checks Updated Profile

```bash
GET /api/rewards/profile
Authorization: Bearer {GUEST_TOKEN}

RESPONSE (200):
{
  "data": {
    "currentPoints": 145,
    "currentTier": {
      "id": 1,
      "name": "Silver Member",
      "pointsThreshold": 0
    },
    "tierProgress": {
      "pointsToNextTier": 205,
      "progressPercentage": 41.43
    },
    "redeemableRewards": [
      {
        "id": 1,
        "title": "Free Coffee",
        "pointsCost": 50,
        "status": "REDEEMABLE"  // Now Redeemable!
      },
      {
        "id": 2,
        "title": "Free Appetizer",
        "pointsCost": 100,
        "status": "REDEEMABLE"  // Now Redeemable!
      },
      {
        "id": 3,
        "title": "Free Dessert Platter",
        "pointsCost": 150,
        "status": "NOT_ENOUGH_POINTS"
      }
    ]
  }
}
```

---

## STEP 11: Guest Redeems Reward Item

### 11.1 Redeem Free Coffee (50 points)

```bash
POST /api/rewards/1/redeem
Authorization: Bearer {GUEST_TOKEN}
Content-Type: application/json

{}

RESPONSE (201):
{
  "success": true,
  "message": "Reward redeemed successfully",
  "data": {
    "redemptionId": 201,
    "redemptionCode": "628374",
    "expiresAt": "2026-09-16T12:00:00Z",
    "newPointsBalance": 95,
    "rewardTitle": "Free Coffee",
    "message": "Your reward is ready! Show code at the counter"
  }
}
```

**✅ SAVE:** `REWARD_REDEMPTION_ID_1 = 201`  
**✅ SAVE:** `REWARD_CODE_1 = "628374"`

### 11.2 Redeem Free Appetizer (100 points)

```bash
POST /api/rewards/2/redeem
Authorization: Bearer {GUEST_TOKEN}
Content-Type: application/json

{}

RESPONSE (201):
{
  "success": true,
  "data": {
    "redemptionId": 202,
    "redemptionCode": "945821",
    "expiresAt": "2026-09-16T12:00:00Z",
    "newPointsBalance": -5,  // Insufficient points
    ...
  }
}
```

**Error Case:** Not enough points

---

## STEP 12: Guest Uploads Receipt & Claims Points

### 12.1 Upload Receipt for Points

```bash
POST /api/rewards/receipt/claim
Authorization: Bearer {GUEST_TOKEN}
Content-Type: multipart/form-data

Form Data:
  - file: <receipt_image.jpg>  // Receipt photo
  - receiptAmount: "75.50"     // Amount on receipt
  - receiptDate: "2026-09-16"  // Date on receipt

RESPONSE (201):
{
  "success": true,
  "message": "Receipt uploaded successfully",
  "data": {
    "claimId": 301,
    "pointsClaimed": 15,
    "newBalance": 80,
    "receiptReference": "RECEIPT_20260916_1",
    "status": "UPLOADED",
    "message": "Receipt submitted for approval. You will earn points once approved.",
    "claimedAt": "2026-09-16T11:15:00Z"
  }
}
```

**✅ SAVE:** `RECEIPT_CLAIM_ID_1 = 301`

---

# 🛠️ ADMIN FLOW - Management Phase

---

## STEP 13: Admin Reviews & Approves Receipt Claims

### 13.1 Get Pending Receipt Claims

```bash
GET /api/admin/receipt-claims/pending?restaurantId=1
Authorization: Bearer {ADMIN_TOKEN}

RESPONSE (200):
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 301,
        "userId": 2,
        "userEmail": "guest@example.com",
        "receiptAmount": "75.50",
        "receiptDate": "2026-09-16",
        "pointsClaimed": 15,
        "status": "UPLOADED",
        "fileUrl": "https://storage.example.com/receipts/UUID123.jpg",
        "createdAt": "2026-09-16T11:15:00Z",
        "updatedAt": "2026-09-16T11:15:00Z"
      }
    ],
    "totalElements": 1,
    "totalPages": 1
  }
}
```

### 13.2 Approve Receipt Claim with Points Override

```bash
PUT /api/admin/receipt-claims/301/approve
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "pointsOverride": 20,
  "notes": "Receipt verified. Adjusted points to 20 for $75.50 purchase."
}

RESPONSE (200):
{
  "success": true,
  "message": "Receipt claim approved successfully",
  "data": {
    "id": 301,
    "userId": 2,
    "status": "APPROVED",
    "pointsClaimed": 20,
    "approvedBy": "admin@restaurant.com",
    "approvedAt": "2026-09-16T11:30:00Z",
    "notes": "Receipt verified. Adjusted points to 20 for $75.50 purchase."
  }
}
```

### 13.3 Guest Verifies Points Were Added

```bash
GET /api/rewards/profile
Authorization: Bearer {GUEST_TOKEN}

RESPONSE (200):
{
  "data": {
    "currentPoints": 100,  // 80 + 20 (approved points)
    "currentTier": {
      "id": 1,
      "name": "Silver Member"
    },
    "tierProgress": {
      "pointsToNextTier": 250,
      "progressPercentage": 28.57
    }
  }
}
```

---

## STEP 14: Admin Manages Redemptions

### 14.1 List All Redemptions

```bash
GET /api/admin/redemptions?page=0&size=20
Authorization: Bearer {ADMIN_TOKEN}

RESPONSE (200):
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 101,
        "redemptionCode": "514527",
        "userId": 2,
        "offerId": 1,
        "offerName": "Happy Hour - 20% Off",
        "status": "COMPLETED",
        "createdAt": "2026-09-16T10:30:00Z",
        "completedAt": "2026-09-16T10:45:00Z",
        "codeExpiresAt": "2026-09-16T11:30:00Z"
      },
      {
        "id": 102,
        "redemptionCode": "823945",
        "userId": 2,
        "offerId": 2,
        "offerName": "Tuesday Deal - $10 Off",
        "status": "COMPLETED",
        ...
      }
    ],
    "totalElements": 2
  }
}
```

### 14.2 Get Redemption by Code

```bash
GET /api/admin/redemptions/by-code/514527
Authorization: Bearer {ADMIN_TOKEN}

RESPONSE (200):
{
  "success": true,
  "data": {
    "id": 101,
    "redemptionCode": "514527",
    "userId": 2,
    "userEmail": "guest@example.com",
    "offerId": 1,
    "offerName": "Happy Hour - 20% Off",
    "status": "COMPLETED",
    "discountValue": 20.0,
    "discountType": "PERCENT",
    "createdAt": "2026-09-16T10:30:00Z",
    "completedAt": "2026-09-16T10:45:00Z"
  }
}
```

### 14.3 Get Redemptions by Status

```bash
GET /api/admin/redemptions/by-status/COMPLETED?page=0&size=20
Authorization: Bearer {ADMIN_TOKEN}

RESPONSE (200):
{
  "success": true,
  "data": {
    "content": [
      { "id": 101, "redemptionCode": "514527", "status": "COMPLETED", ... },
      { "id": 102, "redemptionCode": "823945", "status": "COMPLETED", ... }
    ],
    "totalElements": 2
  }
}
```

---

## STEP 15: Admin Views Analytics & Reports

### 15.1 Offer Statistics

```bash
GET /api/admin/offers/statistics?locationId=1
Authorization: Bearer {ADMIN_TOKEN}

RESPONSE (200):
{
  "success": true,
  "data": {
    "totalOffers": 3,
    "activeOffers": 3,
    "expiredOffers": 0,
    "totalRedemptions": 2,
    "averageRedemptionsPerOffer": 0.67,
    "totalInventoryRemaining": 2699,
    "totalInventoryUsed": 1
  }
}
```

### 15.2 Redemption Statistics

```bash
GET /api/admin/redemptions/statistics?locationId=1
Authorization: Bearer {ADMIN_TOKEN}

RESPONSE (200):
{
  "success": true,
  "data": {
    "totalRedemptions": 2,
    "completedRedemptions": 2,
    "expiredCodes": 0,
    "cancelledRedemptions": 0,
    "generatedCodes": 0,
    "averageRedemptionValue": 15.0,
    "redemptionRate": 100.0
  }
}
```

### 15.3 Receipt Claims Statistics

```bash
GET /api/admin/receipt-claims/statistics?restaurantId=1
Authorization: Bearer {ADMIN_TOKEN}

RESPONSE (200):
{
  "success": true,
  "data": {
    "totalClaims": 1,
    "approved": 1,
    "rejected": 0,
    "pending": 0,
    "duplicate": 0,
    "totalPointsClaimed": 20,
    "averagePointsPerClaim": 20.0,
    "approvalRate": 100.0
  }
}
```

### 15.4 Points Statistics

```bash
GET /api/admin/points/statistics?restaurantId=1
Authorization: Bearer {ADMIN_TOKEN}

RESPONSE (200):
{
  "success": true,
  "data": {
    "totalPointsDistributed": 120,
    "totalPointsRedeemed": 0,
    "averagePointsPerUser": 120,
    "usersWithPoints": 1,
    "topEarnersCount": 1
  }
}
```

### 15.5 Rewards Statistics

```bash
GET /api/admin/rewards/statistics?restaurantId=1
Authorization: Bearer {ADMIN_TOKEN}

RESPONSE (200):
{
  "success": true,
  "data": {
    "totalMembers": 1,
    "totalTiers": 3,
    "totalPointsDistributed": 120,
    "averagePointsPerMember": 120,
    "usersByTier": {
      "Silver": 1,
      "Gold": 0,
      "Platinum": 0
    }
  }
}
```

---

## STEP 16: Export Reports to CSV

### 16.1 Export Offers Report

```bash
GET /api/admin/offers/export?locationId=1
Authorization: Bearer {ADMIN_TOKEN}

RESPONSE (200):
{
  Content-Type: text/csv
  Content-Disposition: attachment; filename=offers_2026-09-16.csv

  CSV Content:
  ID,Name,Status,DiscountType,DiscountValue,Inventory,Redemptions,CreatedAt
  1,"Happy Hour - 20% Off",ACTIVE,PERCENT,20.0,999,1,2026-09-16T10:30:00Z
  2,"Tuesday Deal - $10 Off",ACTIVE,FIXED,10.0,500,1,2026-09-16T10:35:00Z
  3,"Free Dessert Coupon",ACTIVE,FREE_ITEM,0.0,200,0,2026-09-16T10:40:00Z
}
```

### 16.2 Export Redemptions Report

```bash
GET /api/admin/redemptions/export?locationId=1
Authorization: Bearer {ADMIN_TOKEN}

RESPONSE (200):
{
  Content-Type: text/csv
  Content-Disposition: attachment; filename=redemptions_2026-09-16.csv

  CSV Content:
  RedemptionID,Code,OfferName,Status,UserEmail,CreatedAt,CompletedAt
  101,514527,"Happy Hour - 20% Off",COMPLETED,guest@example.com,2026-09-16T10:30:00Z,2026-09-16T10:45:00Z
  102,823945,"Tuesday Deal - $10 Off",COMPLETED,guest@example.com,2026-09-16T10:32:00Z,2026-09-16T10:47:00Z
}
```

---

# 🔄 Advanced Scenarios

---

## Scenario A: Guest Tries to Redeem Without Enough Points

### A.1 Guest Has 50 Points

```bash
GET /api/rewards/profile
Authorization: Bearer {GUEST_TOKEN}

RESPONSE:
{
  "currentPoints": 50,
  "redeemableRewards": [
    {
      "id": 1,
      "title": "Free Coffee",
      "pointsCost": 50,
      "status": "REDEEMABLE"  // Exactly 50 points
    },
    {
      "id": 2,
      "title": "Free Appetizer",
      "pointsCost": 100,
      "status": "NOT_ENOUGH_POINTS"  // Needs 100
    }
  ]
}
```

### A.2 Attempt to Redeem Insufficient Points

```bash
POST /api/rewards/2/redeem
Authorization: Bearer {GUEST_TOKEN}

RESPONSE (400):
{
  "success": false,
  "message": "Insufficient points. Required: 100, Available: 50",
  "error": "INSUFFICIENT_POINTS"
}
```

---

## Scenario B: Duplicate Receipt Detection

### B.1 Guest Uploads First Receipt

```bash
POST /api/rewards/receipt/claim
Authorization: Bearer {GUEST_TOKEN}
Content-Type: multipart/form-data

Form Data:
  - file: receipt1.jpg
  - receiptAmount: "75.50"
  - receiptDate: "2026-09-16"

RESPONSE (201):
{
  "status": "UPLOADED",
  "claimId": 301
}
```

### B.2 Guest Tries to Upload Same Receipt Again (Within 24 Hours)

```bash
POST /api/rewards/receipt/claim
Authorization: Bearer {GUEST_TOKEN}
Content-Type: multipart/form-data

Form Data:
  - file: receipt1.jpg  // Same receipt
  - receiptAmount: "75.50"
  - receiptDate: "2026-09-16"

RESPONSE (409):
{
  "success": false,
  "message": "Duplicate receipt detected. Identical receipt already claimed.",
  "error": "DUPLICATE_RECEIPT"
}
```

### B.3 Admin Marks as Duplicate

```bash
POST /api/admin/receipt-claims/301/mark-duplicate
Authorization: Bearer {ADMIN_TOKEN}

RESPONSE (200):
{
  "status": "DUPLICATE",
  "reason": "System detected duplicate"
}
```

---

## Scenario C: Offer Inventory Depleted

### C.1 Check Offer with Low Inventory

```bash
GET /api/admin/offers/low-inventory?locationId=1&threshold=10
Authorization: Bearer {ADMIN_TOKEN}

RESPONSE (200):
{
  "data": {
    "content": [
      {
        "id": 3,
        "name": "Free Dessert Coupon",
        "inventory": 5,
        "alert": true,
        "message": "Only 5 items remaining!"
      }
    ]
  }
}
```

### C.2 Update Inventory

```bash
POST /api/admin/offers/bulk-update-inventory
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "updates": [
    { "offerId": 3, "newInventory": 500 }
  ]
}

RESPONSE (200):
{
  "updated": 1,
  "total": 1
}
```

---

## Scenario D: Offer Expires

### D.1 Get Expiring Soon Offers

```bash
GET /api/admin/offers/expiring-soon?locationId=1&days=7
Authorization: Bearer {ADMIN_TOKEN}

RESPONSE (200):
{
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Happy Hour - 20% Off",
        "endDate": "2026-09-20",
        "daysUntilExpiry": 4,
        "alert": true
      }
    ]
  }
}
```

### D.2 Guest Cannot Redeem Expired Offer

```bash
POST /api/offers/1/redeem
Authorization: Bearer {GUEST_TOKEN}

RESPONSE (409):
{
  "success": false,
  "message": "This offer has expired",
  "error": "OFFER_EXPIRED"
}
```

---

## Scenario E: Admin Bulk Operations

### E.1 Bulk Credit Points to Multiple Users

```bash
POST /api/admin/points/bulk-credit
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "userIds": [2, 3, 4, 5],
  "amount": 50,
  "reason": "Monthly loyalty bonus"
}

RESPONSE (200):
{
  "success": true,
  "data": {
    "totalUsers": 4,
    "pointsPerUser": 50,
    "totalPointsDistributed": 200,
    "timestamp": "2026-09-16T12:00:00Z"
  }
}
```

### E.2 Bulk Approve Receipt Claims

```bash
POST /api/admin/receipt-claims/approve-bulk
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "claimIds": [301, 302, 303],
  "pointsOverride": 20
}

RESPONSE (200):
{
  "success": true,
  "data": {
    "totalRequested": 3,
    "approved": 3,
    "failed": 0
  }
}
```

---

# ✅ Verification Checklist

After completing all steps, verify:

- [ ] 3 offers created with different discount types
- [ ] 3 reward tiers created (Silver, Gold, Platinum)
- [ ] 3 reward items created
- [ ] 3 earning rules configured
- [ ] Guest received 6-digit offer redemption codes
- [ ] Guest viewed rewards profile with tier progression
- [ ] Admin credited points to guest
- [ ] Guest redeemed reward item successfully
- [ ] Guest uploaded receipt and claimed points
- [ ] Admin approved receipt claim
- [ ] All analytics and statistics displayed correctly
- [ ] CSV exports generated successfully
- [ ] Error scenarios tested (insufficient points, duplicates, expired)

---

# 🔗 API Reference Links

**Swagger UI:** http://localhost:8080/swagger-ui/index.html  
**API Docs:** http://localhost:8080/v3/api-docs  
**Admin Endpoints:** ADMIN_ENDPOINTS_DOCUMENTATION.md  
**Complete Implementation:** IMPLEMENTATION_SUMMARY.md

---

## 📝 Notes

- All timestamps are in ISO 8601 format (UTC)
- Redemption codes are 6-digit numeric strings
- Points are awarded based on configured earning rules
- Receipt claims require approval before points are credited
- Duplicate detection is based on: user + restaurant + amount + date within 24 hours
- All operations are logged for audit purposes

---

**Last Updated:** 2026-09-16  
**Testing Status:** Complete E2E Flow Documented ✅

