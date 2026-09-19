# Rewards & Points Flow - API Testing Guide

## System Overview

The Dinerly Rewards system consists of three main components:

1. **Rewards System** - Points-based rewards that users can redeem for items
2. **Offers System** - Time-limited discount offers available at restaurants
3. **Points System** - Points earning and tracking through various actions

---

## Table of Contents

1. [Authentication Setup](#authentication-setup)
2. [Rewards System](#rewards-system)
3. [Offers System](#offers-system)
4. [Points System](#points-system)
5. [Complete Testing Workflow](#complete-testing-workflow)

---

## Authentication Setup

Before testing any endpoints, you need to:

1. Register a user
2. Login to get authentication token
3. Use the token in Authorization header

### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1,
    "email": "user@example.com"
  }
}
```

**Use this token for all subsequent requests:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## Rewards System

The rewards system allows guests to redeem points for items like free coffee, discounts, etc.

### 1. Get Rewards Profile

**Endpoint:**
```http
GET /api/rewards/profile
Authorization: Bearer {token}
```

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| restaurantId | Long | No | Restaurant ID (defaults to 1) |

**Request Example:**
```http
GET /api/rewards/profile?restaurantId=1
Authorization: Bearer {token}
Content-Type: application/json
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Rewards profile retrieved successfully",
  "data": {
    "currentPoints": 500,
    "currentTier": {
      "id": 2,
      "name": "Gold",
      "color": "gold",
      "pointsThreshold": 350,
      "tierOrder": 2
    },
    "tierProgress": {
      "pointsToNextTier": 200,
      "nextTierName": "Platinum",
      "progressPercentage": 42.85,
      "nextTierOrder": 3
    },
    "redeemableRewards": [
      {
        "id": 1,
        "title": "Free Coffee",
        "description": "Any size, any blend",
        "pointsCost": 100,
        "icon": "coffee-icon-url",
        "status": "REDEEMABLE"
      },
      {
        "id": 2,
        "title": "$5 Off Food",
        "description": "Valid on any food item",
        "pointsCost": 250,
        "icon": "discount-icon-url",
        "status": "REDEEMABLE"
      },
      {
        "id": 3,
        "title": "Free dessert",
        "description": "One dessert item",
        "pointsCost": 500,
        "icon": "dessert-icon-url",
        "status": "NOT_ENOUGH_POINTS"
      }
    ],
    "waysToEarn": [
      {
        "action": "join_waitlist",
        "title": "Join the waitlist",
        "subtitle": "Earn points for joining",
        "pointsValue": 10,
        "clickable": true,
        "actionUrl": "/waitlist",
        "icon": "waitlist-icon-url"
      },
      {
        "action": "dine_in",
        "title": "Dined without joining waitlist?",
        "subtitle": "Claim your points",
        "pointsValue": 50,
        "clickable": true,
        "actionUrl": "/claim-receipt",
        "icon": "receipt-icon-url"
      },
      {
        "action": "leave_review",
        "title": "Leave a review",
        "subtitle": "Share your experience",
        "pointsValue": 25,
        "clickable": true,
        "actionUrl": "/reviews",
        "icon": "review-icon-url"
      },
      {
        "action": "refer_friend",
        "title": "Refer a friend",
        "subtitle": "Earn bonus points",
        "pointsValue": 100,
        "clickable": true,
        "actionUrl": "/referral",
        "icon": "refer-icon-url"
      }
    ]
  }
}
```

**Response (400 Bad Request):**
```json
{
  "success": false,
  "message": "User not authenticated"
}
```

---

### 2. Redeem a Reward Item

**Endpoint:**
```http
POST /api/rewards/{rewardId}/redeem
Authorization: Bearer {token}
Content-Type: application/json
```

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| rewardId | Long | Yes | ID of the reward item to redeem |

**Request Body:**
```json
{
  "restaurantId": 1
}
```

**Request Example:**
```http
POST /api/rewards/1/redeem
Authorization: Bearer {token}
Content-Type: application/json

{
  "restaurantId": 1
}
```

**Response (200 OK) - Successful Redemption:**
```json
{
  "success": true,
  "message": "Reward redeemed successfully",
  "data": {
    "redemptionId": 101,
    "redemptionCode": "A7F3K2",
    "expiresAt": "2024-09-19T15:45:00",
    "message": "Your reward code: A7F3K2",
    "newPointsBalance": 400,
    "rewardTitle": "Free Coffee"
  }
}
```

**Response (400 Bad Request) - Insufficient Points:**
```json
{
  "success": false,
  "message": "Insufficient points. Need 500 but have 400"
}
```

**Response (400 Bad Request) - Reward Not Found:**
```json
{
  "success": false,
  "message": "Reward item not found"
}
```

**Response (400 Bad Request) - Reward Not Available:**
```json
{
  "success": false,
  "message": "Reward is not available"
}
```

**Response (401 Unauthorized):**
```json
{
  "success": false,
  "message": "User not authenticated"
}
```

**Important Notes:**
- The 6-digit redemption code expires in 1 hour
- Points are deducted immediately upon successful redemption
- Guest must show the code to staff for verification
- Each redemption is tracked in the points ledger

---

## Offers System

The offers system allows guests to redeem time-limited discount offers.

### 1. List Available Offers

**Endpoint:**
```http
GET /api/offers
Authorization: Bearer {token}
```

**Query Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| locationId | Long | No | null | Filter by restaurant location |
| category | String | No | null | Filter by offer category |
| page | Integer | No | 0 | Page number (0-indexed) |
| size | Integer | No | 20 | Items per page |

**Request Examples:**

**Get all offers (default pagination):**
```http
GET /api/offers
Authorization: Bearer {token}
Content-Type: application/json
```

**Get offers for specific location:**
```http
GET /api/offers?locationId=1
Authorization: Bearer {token}
```

**Get offers with pagination:**
```http
GET /api/offers?locationId=1&page=0&size=10
Authorization: Bearer {token}
```

**Get offers filtered by category:**
```http
GET /api/offers?locationId=1&category=food&page=0&size=10
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Offers retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "20% Off Food",
        "restaurantId": 1,
        "restaurantName": "Italian House",
        "startDate": "2024-09-01",
        "endDate": "2024-09-30",
        "status": "ACTIVE",
        "discountValue": 20,
        "discountLabel": "20% off",
        "description": "Get 20% off on all food items with minimum purchase of $50",
        "photoUrl": "https://example.com/offer-image.jpg",
        "redeemable": true,
        "reasonIfNotRedeemable": null,
        "userRedemptionsTotal": 0
      },
      {
        "id": 2,
        "name": "Free Appetizer",
        "restaurantId": 1,
        "restaurantName": "Italian House",
        "startDate": "2024-09-10",
        "endDate": "2024-09-25",
        "status": "ACTIVE",
        "discountValue": 15,
        "discountLabel": "Free appetizer",
        "description": "Complimentary appetizer with any main course",
        "photoUrl": "https://example.com/offer-image2.jpg",
        "redeemable": false,
        "reasonIfNotRedeemable": "You have already redeemed this offer 1 times",
        "userRedemptionsTotal": 1
      },
      {
        "id": 3,
        "name": "Happy Hour Special",
        "restaurantId": 1,
        "restaurantName": "Italian House",
        "startDate": "2024-09-15",
        "endDate": "2024-09-20",
        "status": "ACTIVE",
        "discountValue": 30,
        "discountLabel": "$5 off drinks",
        "description": "$5 off any beverages during happy hour",
        "photoUrl": "https://example.com/offer-image3.jpg",
        "redeemable": true,
        "reasonIfNotRedeemable": null,
        "userRedemptionsTotal": 0
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20,
      "totalElements": 3,
      "totalPages": 1
    }
  }
}
```

**Response (400 Bad Request):**
```json
{
  "success": false,
  "message": "Error fetching offers"
}
```

---

### 2. Get Offer Details

**Endpoint:**
```http
GET /api/offers/{offerId}
Authorization: Bearer {token}
```

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| offerId | Long | Yes | ID of the offer |

**Request Example:**
```http
GET /api/offers/1
Authorization: Bearer {token}
Content-Type: application/json
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Offer details retrieved successfully",
  "data": {
    "id": 1,
    "name": "20% Off Food",
    "restaurantId": 1,
    "restaurantName": "Italian House",
    "startDate": "2024-09-01",
    "endDate": "2024-09-30",
    "status": "ACTIVE",
    "discountValue": 20,
    "discountLabel": "20% off",
    "description": "Get 20% off on all food items with minimum purchase of $50",
    "photoUrl": "https://example.com/offer-image.jpg",
    "redeemable": true,
    "reasonIfNotRedeemable": null,
    "userRedemptionsTotal": 0
  }
}
```

**Response (404 Not Found):**
```json
{
  "success": false,
  "message": null
}
```

---

### 3. Redeem an Offer

**Endpoint:**
```http
POST /api/offers/{offerId}/redeem
Authorization: Bearer {token}
```

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| offerId | Long | Yes | ID of the offer to redeem |

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| locationId | Long | No | Restaurant/location ID (defaults to 1) |

**Request Examples:**

**Redeem offer with default location:**
```http
POST /api/offers/1/redeem
Authorization: Bearer {token}
Content-Type: application/json
```

**Redeem offer for specific location:**
```http
POST /api/offers/1/redeem?locationId=1
Authorization: Bearer {token}
Content-Type: application/json
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Offer redeemed successfully",
  "data": {
    "redemptionId": 42,
    "redemptionCode": "X9M2Q8",
    "expiresAt": "2024-09-19T16:30:00",
    "message": "Show this code to your server",
    "offerName": "20% Off Food",
    "offerDescription": "Get 20% off on all food items with minimum purchase of $50"
  }
}
```

**Response (400 Bad Request) - Offer Not Active:**
```json
{
  "success": false,
  "message": "Offer is not active"
}
```

**Response (400 Bad Request) - Offer Expired:**
```json
{
  "success": false,
  "message": "Offer has expired"
}
```

**Response (400 Bad Request) - Exceeded Limit:**
```json
{
  "success": false,
  "message": "You have already redeemed this offer 1 times"
}
```

**Response (400 Bad Request) - Offer Not Found:**
```json
{
  "success": false,
  "message": "Offer not found"
}
```

**Important Notes:**
- The redemption code is 6 digits, generated uniquely
- Code expires in 1 hour
- Guest must show code to staff for validation
- Tracks number of times user has redeemed the same offer

---

### 4. Validate & Complete Offer Code (Staff Only)

**Endpoint:**
```http
POST /api/offers/redeem/{code}/confirm
Authorization: Bearer {token}
```

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| code | String | Yes | The 6-digit redemption code |

**Required Role:** STAFF or ADMIN

**Request Example:**
```http
POST /api/offers/redeem/X9M2Q8/confirm
Authorization: Bearer {token}
Content-Type: application/json
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Code validated and redeemed",
  "data": {
    "redemptionId": 42,
    "redemptionCode": "X9M2Q8",
    "status": "COMPLETED",
    "offerName": "20% Off Food",
    "offerDescription": "Get 20% off on all food items with minimum purchase of $50",
    "userId": 5,
    "userEmail": "user@example.com",
    "mobileNumber": "+1234567890",
    "offerId": 1,
    "discountType": "FIXED",
    "discountValue": 20,
    "discountLabel": "20% off",
    "confirmedAt": "2024-09-19T15:45:32",
    "codeExpiresAt": "2024-09-19T16:30:00"
  }
}
```

**Response (400 Bad Request) - Invalid/Expired Code:**
```json
{
  "success": false,
  "message": "Invalid or expired redemption code"
}
```

**Response (403 Forbidden) - Insufficient Role:**
```json
{
  "success": false,
  "message": "Access denied"
}
```

**Redemption Code Status Values:**
- `GENERATED` - Code generated, shown to guest
- `COMPLETED` - Staff entered code, offer applied
- `EXPIRED` - Code TTL exceeded (1 hour)
- `CANCELLED` - Guest cancelled before use

---

## Points System

The points system tracks how users earn and spend points.

### 1. Get Rewards Profile (Includes Points)

Already covered in [Rewards System - Get Rewards Profile](#1-get-rewards-profile)

The `currentPoints` field shows the user's total points balance.

---

### 2. Claim Receipt for Points

**Endpoint:**
```http
POST /api/rewards/receipt/claim
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

**Query/Form Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| file | File | Yes | Receipt image file (jpg, png, pdf) |
| restaurantId | Long | Yes | Restaurant where meal was had |
| receiptAmount | String | No | Receipt total amount (from OCR or manual) |
| receiptDate | String | No | Receipt date (YYYY-MM-DD format) |

**Request Example (using cURL):**
```bash
curl -X POST http://localhost:8080/api/rewards/receipt/claim \
  -H "Authorization: Bearer {token}" \
  -F "file=@receipt.jpg" \
  -F "restaurantId=1" \
  -F "receiptAmount=45.50" \
  -F "receiptDate=2024-09-19"
```

**Request Example (using Postman):**
```
POST /api/rewards/receipt/claim
Authorization: Bearer {token}
Content-Type: multipart/form-data

Body:
- file: [image file: receipt.jpg]
- restaurantId: 1
- receiptAmount: 45.50
- receiptDate: 2024-09-19
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Receipt claimed successfully",
  "data": {
    "pointsClaimed": 50,
    "newBalance": 550,
    "receiptReference": "RCP-2024-09-19-12345",
    "status": "APPROVED",
    "message": "Points approved and added to your account",
    "claimedAt": "2024-09-19T14:30:15"
  }
}
```

**Response (200 OK) - Pending Review:**
```json
{
  "success": true,
  "message": "Receipt claimed successfully",
  "data": {
    "pointsClaimed": 50,
    "newBalance": 500,
    "receiptReference": "RCP-2024-09-19-12346",
    "status": "PENDING_REVIEW",
    "message": "Your receipt is under review. Points will be added within 24 hours",
    "claimedAt": "2024-09-19T14:35:20"
  }
}
```

**Response (400 Bad Request) - File Error:**
```json
{
  "success": false,
  "message": "Error processing file: File size exceeds maximum limit"
}
```

**Response (400 Bad Request) - Invalid Input:**
```json
{
  "success": false,
  "message": "Unable to parse receipt details"
}
```

**Response (401 Unauthorized):**
```json
{
  "success": false,
  "message": "User not authenticated"
}
```

**Points Earning Rules (Examples):**
- Join Waitlist: 10 points
- Dine-in (receipt claim): 50-100 points (based on amount)
- Leave Review: 25 points
- Refer a Friend: 100 points

---

## Reward Tiers

Users progress through reward tiers based on accumulated points.

### Tier Progression

```
Tier Level    | Points Required | Benefits
-----------   | --------------- | --------
Silver        | 0+              | Base tier
Gold          | 350+            | Priority seating
Platinum      | 700+            | VIP treatment, exclusive offers
```

### How Tier Progress Works:

1. **Silver Tier** (0-349 points)
   - Starting tier for all users
   - Earn 50 points from receipt claim, 10 from joining waitlist

2. **Gold Tier** (350-699 points)
   - Unlock at 350 points
   - Better redeem options available
   - Progress example: At 500 points, you're 42.85% towards Platinum

3. **Platinum Tier** (700+ points)
   - Maximum tier
   - Exclusive rewards and perks

### Checking Tier Progress:

From the Rewards Profile response, you can check:
```json
{
  "currentTier": {
    "name": "Gold",
    "pointsThreshold": 350,
    "tierOrder": 2
  },
  "tierProgress": {
    "pointsToNextTier": 200,
    "nextTierName": "Platinum",
    "progressPercentage": 42.85,
    "nextTierOrder": 3
  }
}
```

---

## Complete Testing Workflow

This section provides a complete end-to-end testing scenario.

### Scenario: User Earns and Redeems Points for a Reward

#### Step 1: User Registers and Logs In

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "testuser@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1
  }
}
```

Save token: `TOKEN=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`

---

#### Step 2: Check Initial Rewards Profile

```http
GET /api/rewards/profile?restaurantId=1
Authorization: Bearer {TOKEN}
```

**Initial Response:**
```json
{
  "data": {
    "currentPoints": 0,
    "currentTier": {
      "name": "Silver",
      "pointsThreshold": 0
    },
    "tierProgress": {
      "pointsToNextTier": 350,
      "nextTierName": "Gold",
      "progressPercentage": 0.0
    },
    "redeemableRewards": [
      {
        "id": 1,
        "title": "Free Coffee",
        "pointsCost": 100,
        "status": "NOT_ENOUGH_POINTS"
      }
    ]
  }
}
```

**Observation:** User has 0 points, cannot redeem anything yet.

---

#### Step 3: List Available Offers

```http
GET /api/offers?locationId=1&page=0&size=10
Authorization: Bearer {TOKEN}
```

**Response:**
```json
{
  "data": {
    "content": [
      {
        "id": 1,
        "name": "20% Off Food",
        "redeemable": true,
        "userRedemptionsTotal": 0
      },
      {
        "id": 2,
        "name": "Free Appetizer",
        "redeemable": true,
        "userRedemptionsTotal": 0
      }
    ]
  }
}
```

---

#### Step 4: Claim Receipt to Earn Points

User dines at the restaurant and uploads receipt.

```http
POST /api/rewards/receipt/claim
Authorization: Bearer {TOKEN}
Content-Type: multipart/form-data

Body:
- file: [receipt image]
- restaurantId: 1
- receiptAmount: 85.50
- receiptDate: 2024-09-19
```

**Response:**
```json
{
  "success": true,
  "data": {
    "pointsClaimed": 85,
    "newBalance": 85,
    "status": "APPROVED",
    "claimedAt": "2024-09-19T14:30:15"
  }
}
```

**Progress:** User now has 85 points (towards 350 for Gold tier)

---

#### Step 5: Check Updated Rewards Profile

```http
GET /api/rewards/profile?restaurantId=1
Authorization: Bearer {TOKEN}
```

**Updated Response:**
```json
{
  "data": {
    "currentPoints": 85,
    "currentTier": {
      "name": "Silver",
      "pointsThreshold": 0
    },
    "tierProgress": {
      "pointsToNextTier": 265,
      "nextTierName": "Gold",
      "progressPercentage": 24.3
    },
    "redeemableRewards": [
      {
        "id": 1,
        "title": "Free Coffee",
        "pointsCost": 100,
        "status": "NOT_ENOUGH_POINTS"
      }
    ]
  }
}
```

**Observation:** Still need 15 more points to redeem Free Coffee. Need 265 more for Gold tier.

---

#### Step 6: Redeem an Offer (Different Offer Track)

User wants to use a time-limited offer instead of waiting for points.

```http
POST /api/offers/1/redeem?locationId=1
Authorization: Bearer {TOKEN}
```

**Response:**
```json
{
  "success": true,
  "message": "Offer redeemed successfully",
  "data": {
    "redemptionId": 42,
    "redemptionCode": "A7F3K2",
    "expiresAt": "2024-09-19T15:45:00",
    "message": "Show this code to your server",
    "offerName": "20% Off Food",
    "offerDescription": "Get 20% off on all food items with minimum purchase of $50"
  }
}
```

**User gets code: A7F3K2** (expires in 1 hour)

---

#### Step 7: Staff Validates Offer Code at Register

Staff member at restaurant validates the code.

```http
POST /api/offers/redeem/A7F3K2/confirm
Authorization: Bearer {STAFF_TOKEN}
```

**Response:**
```json
{
  "success": true,
  "message": "Code validated and redeemed",
  "data": {
    "redemptionCode": "A7F3K2",
    "status": "COMPLETED",
    "offerName": "20% Off Food",
    "discountValue": 20,
    "confirmedAt": "2024-09-19T15:44:32"
  }
}
```

**Result:** Offer is applied to the bill.

---

#### Step 8: Check Updated Rewards Profile Again

```http
GET /api/rewards/profile?restaurantId=1
Authorization: Bearer {TOKEN}
```

**Response:**
```json
{
  "data": {
    "currentPoints": 85,
    "currentTier": {
      "name": "Silver",
      "pointsThreshold": 0
    }
  }
}
```

**Note:** Points don't change when redeeming offers. Offers are different from reward redemptions.

---

#### Step 9: Earn More Points - Join Waitlist

User joins waitlist at the restaurant.

**System automatically credits:** 10 points

```http
GET /api/rewards/profile?restaurantId=1
Authorization: Bearer {TOKEN}
```

**Updated Response:**
```json
{
  "data": {
    "currentPoints": 95,
    "currentTier": {
      "name": "Silver",
      "pointsThreshold": 0
    },
    "tierProgress": {
      "pointsToNextTier": 255,
      "nextTierName": "Gold",
      "progressPercentage": 27.1
    }
  }
}
```

---

#### Step 10: Earn More Points - Leave Review

User leaves a review.

**System automatically credits:** 25 points

```http
GET /api/rewards/profile?restaurantId=1
Authorization: Bearer {TOKEN}
```

**Updated Response:**
```json
{
  "data": {
    "currentPoints": 120,
    "currentTier": {
      "name": "Silver",
      "pointsThreshold": 0
    },
    "tierProgress": {
      "pointsToNextTier": 230,
      "nextTierName": "Gold",
      "progressPercentage": 34.3
    }
  }
}
```

**Progress:** User now has 120 points, can redeem Free Coffee (costs 100)!

---

#### Step 11: Redeem a Reward Item

User wants to redeem Free Coffee reward (costs 100 points).

```http
POST /api/rewards/1/redeem
Authorization: Bearer {TOKEN}
Content-Type: application/json

{
  "restaurantId": 1
}
```

**Response:**
```json
{
  "success": true,
  "message": "Reward redeemed successfully",
  "data": {
    "redemptionCode": "K9L2M5",
    "expiresAt": "2024-09-19T16:20:00",
    "message": "Your reward code: K9L2M5",
    "newPointsBalance": 20,
    "rewardTitle": "Free Coffee"
  }
}
```

**User gets code: K9L2M5** (expires in 1 hour)
**Points deducted:** 100 → Remaining: 20 points

---

#### Step 12: Check Final Rewards Profile

```http
GET /api/rewards/profile?restaurantId=1
Authorization: Bearer {TOKEN}
```

**Final Response:**
```json
{
  "data": {
    "currentPoints": 20,
    "currentTier": {
      "name": "Silver",
      "pointsThreshold": 0
    },
    "tierProgress": {
      "pointsToNextTier": 330,
      "nextTierName": "Gold",
      "progressPercentage": 5.7
    },
    "redeemableRewards": [
      {
        "id": 1,
        "title": "Free Coffee",
        "pointsCost": 100,
        "status": "NOT_ENOUGH_POINTS"
      }
    ]
  }
}
```

**Summary:**
- Started with: 0 points
- Earned from receipt: 85 points
- Earned from waitlist: 10 points
- Earned from review: 25 points
- Total earned: 120 points
- Redeemed reward: -100 points
- Final balance: 20 points
- Progress: 5.7% towards Gold tier

---

## Testing Tips & Common Scenarios

### Success Criteria

✅ **Rewards Profile Testing:**
- Points balance is accurate
- Tier information matches points
- Redeemable rewards show correct status
- Ways to earn are populated

✅ **Offer Redemption Testing:**
- Code is 6 digits
- Code expires in 1 hour
- Cannot redeem expired offer
- Respects per-user limit
- Cannot redeem inactive offers

✅ **Reward Redemption Testing:**
- Can only redeem with enough points
- Code is generated
- Points deducted immediately
- New balance is correct
- Cannot redeem unavailable items

✅ **Points Earning Testing:**
- Receipt claims award points
- Waitlist join awards points
- Points accumulate correctly
- Ledger tracks all transactions

### Common Error Scenarios

| Scenario | Expected HTTP Status | Error Message |
|----------|----------------------|---------------|
| Offer not found | 400 | "Offer not found" |
| Offer expired | 400 | "Offer has expired" |
| User limit exceeded | 400 | "You have already redeemed this offer X times" |
| Insufficient points | 400 | "Insufficient points. Need X but have Y" |
| Invalid code | 400 | "Invalid or expired redemption code" |
| Unauthenticated | 401 | "User not authenticated" |
| Wrong role | 403 | "Access denied" |

---

## Data Model Reference

### DinerlyPoints Table
```
- userId: Long (unique)
- balance: Long (in points)
- version: Long (optimistic locking)
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

### PointsLedger Table
```
- id: Long (primary key)
- userId: Long
- delta: Long (positive or negative)
- balanceAfter: Long
- reason: String
- sourceType: String (reward_redemption, receipt_claim, etc.)
- sourceId: Long
- createdAt: LocalDateTime
```

### Redemption Table
```
- id: Long
- offerId: Long (foreign key)
- restaurantId: Long
- userId: Long
- redemptionCode: String (unique, 6 digits)
- status: RedemptionStatus (GENERATED, COMPLETED, EXPIRED, CANCELLED)
- codeExpiresAt: LocalDateTime (1 hour from generation)
- redeemedAt: LocalDateTime
- value: BigDecimal
- guestName: String
- guestPhone: String
- updatedAt: LocalDateTime
```

### RewardItem Table
```
- id: Long
- restaurantId: Long (foreign key)
- title: String
- description: String
- pointsCost: Long
- icon: String
- category: String (food, beverage, discount)
- available: Boolean
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

### Offer Table
```
- id: Long
- restaurantId: Long (foreign key)
- name: String
- startDate: LocalDate
- endDate: LocalDate
- status: String (ACTIVE, INACTIVE, DRAFT)
- discountValue: BigDecimal
- discountLabel: String
- description: String
- photoUrl: String
- perUserLimit: Integer (max times one user can redeem)
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

### RewardTier Table
```
- id: Long
- restaurantId: Long (foreign key)
- name: String (Silver, Gold, Platinum)
- pointsThreshold: Long
- tierOrder: Integer (1=Silver, 2=Gold, 3=Platinum)
- perks: List<String>
- color: String (silver, gold, platinum)
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

---

## Postman Collection Template

### Import these as requests in Postman:

**Environment Variables to set:**
```
{{base_url}} = http://localhost:8080
{{token}} = [your-jwt-token]
{{restaurantId}} = 1
{{offerId}} = 1
{{rewardId}} = 1
{{redemptionCode}} = [generated-code]
```

**Sample Requests:**

```
GET {{base_url}}/api/rewards/profile?restaurantId={{restaurantId}}
GET {{base_url}}/api/offers?locationId={{restaurantId}}&page=0&size=10
POST {{base_url}}/api/rewards/{{rewardId}}/redeem
POST {{base_url}}/api/offers/{{offerId}}/redeem?locationId={{restaurantId}}
POST {{base_url}}/api/offers/redeem/{{redemptionCode}}/confirm
POST {{base_url}}/api/rewards/receipt/claim
```

---

## Notes

- All timestamps are in UTC
- Redemption codes are 6 alphanumeric characters
- Code validity: 1 hour from generation
- Points are stored as Long integers
- Decimal discounts are BigDecimal for accuracy
- User authentication is required for most endpoints
- Staff role required for code confirmation endpoint
- Receipt claim may result in PENDING_REVIEW status for human verification


