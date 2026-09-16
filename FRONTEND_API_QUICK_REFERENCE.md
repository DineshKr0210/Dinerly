# Dinerly API Quick Reference
## Offers, Rewards & Points Endpoints

---

## Authentication
```
POST   /api/auth/login                    - Login (guest/admin/staff)
POST   /api/auth/register                 - Register new guest
POST   /api/auth/logout                   - Logout
POST   /api/auth/refresh-token            - Refresh JWT token
```

---

## OFFERS - ADMIN ENDPOINTS

### Create & Manage
```
POST   /api/admin/offers                  - Create new offer
GET    /api/admin/offers                  - List all offers (with filters)
GET    /api/admin/offers/{offerId}        - Get single offer details
PUT    /api/admin/offers/{offerId}        - Update offer
DELETE /api/admin/offers/{offerId}        - Delete offer
```

### Offer Status & Operations
```
PUT    /api/admin/offers/{offerId}/toggle-status      - Toggle ACTIVE/INACTIVE
POST   /api/admin/offers/{offerId}/duplicate          - Duplicate offer
PUT    /api/admin/offers/{offerId}/archive            - Archive offer
POST   /api/admin/offers/bulk-duplicate               - Bulk duplicate
POST   /api/admin/offers/bulk-archive                 - Bulk archive
```

### Offer Analytics
```
GET    /api/admin/offers/categories                   - Get all categories
GET    /api/admin/offers/by-category/{category}       - Filter by category
GET    /api/admin/offers/expiring-soon                - Offers expiring soon
GET    /api/admin/offers/low-inventory                - Low inventory offers
GET    /api/admin/offers/statistics                   - Offer statistics
GET    /api/admin/offers/export                       - Export to CSV
```

---

## OFFERS - GUEST ENDPOINTS

```
GET    /api/offers                        - List available offers
GET    /api/offers/{offerId}              - Get offer details
POST   /api/offers/{offerId}/redeem       - Redeem offer (generate code)
POST   /api/offers/redeem/{code}/confirm  - Validate code (staff/admin)
```

---

## REDEMPTIONS - ADMIN ENDPOINTS

```
GET    /api/admin/redemptions             - List all redemptions
GET    /api/admin/redemptions/{id}        - Get redemption details
GET    /api/admin/redemptions/by-code/{code} - Get by code
POST   /api/admin/redemptions/{id}/confirm   - Confirm redemption
POST   /api/admin/redemptions/{id}/cancel    - Cancel redemption
GET    /api/admin/redemptions/export      - Export to CSV
GET    /api/admin/redemptions/statistics  - Redemption statistics
```

---

## REWARDS - ADMIN ENDPOINTS

### Tier Management
```
GET    /api/admin/rewards/tiers                    - List all tiers
POST   /api/admin/rewards/tiers                    - Create tier
PUT    /api/admin/rewards/tiers/{tierId}           - Update tier
DELETE /api/admin/rewards/tiers/{tierId}           - Delete tier
GET    /api/admin/rewards/tiers/{tierId}           - Get tier details
PUT    /api/admin/rewards/tiers/{tierId}/duplicate - Duplicate tier
```

### Ways to Earn
```
GET    /api/admin/rewards/ways-to-earn             - List all ways to earn
POST   /api/admin/rewards/ways-to-earn             - Create way to earn
PUT    /api/admin/rewards/ways-to-earn/{ruleId}    - Update rule
DELETE /api/admin/rewards/ways-to-earn/{ruleId}    - Delete rule
```

### Settings & Analytics
```
GET    /api/admin/rewards/settings                        - Get settings
PUT    /api/admin/rewards/settings                        - Update settings
GET    /api/admin/rewards/statistics                      - Rewards statistics
GET    /api/admin/rewards/user-tier-distribution          - Tier distribution
```

---

## REWARD ITEMS - ADMIN ENDPOINTS

### CRUD Operations
```
GET    /api/admin/reward-items                     - List reward items
GET    /api/admin/reward-items/{itemId}            - Get item details
POST   /api/admin/reward-items                     - Create reward item
PUT    /api/admin/reward-items/{itemId}            - Update item
DELETE /api/admin/reward-items/{itemId}            - Delete item
```

### Item Management
```
PUT    /api/admin/reward-items/{itemId}/toggle-availability  - Toggle availability
POST   /api/admin/reward-items/bulk-toggle-availability      - Bulk toggle
GET    /api/admin/reward-items/category/{category}           - Filter by category
GET    /api/admin/reward-items/categories                    - List all categories
POST   /api/admin/reward-items/{itemId}/duplicate            - Duplicate item
```

---

## REWARDS - GUEST ENDPOINTS

```
GET    /api/rewards/profile               - Get rewards profile & tier
POST   /api/rewards/{rewardId}/redeem    - Redeem reward item
POST   /api/rewards/receipt/claim         - Claim receipt for points
```

---

## POINTS - ADMIN ENDPOINTS

### Credit/Debit Operations
```
POST   /api/admin/points/credit           - Credit points to user
POST   /api/admin/points/debit            - Debit points from user
POST   /api/admin/points/reverse          - Reverse points transaction
POST   /api/admin/points/bulk-credit      - Bulk credit to multiple users
POST   /api/admin/points/bulk-reverse     - Bulk reverse transactions
POST   /api/admin/points/set-balance      - Set exact balance
```

### Inquiry & Analytics
```
GET    /api/admin/points/balance/{userId}         - Get user balance
GET    /api/admin/points/ledger/{userId}          - Get points ledger/history
GET    /api/admin/points/statistics               - Points statistics
GET    /api/admin/points/top-earners              - Top earners list
```

---

## POINTS EARNING RULES - ADMIN ENDPOINTS

### Rule Management
```
GET    /api/admin/points/earning-rules                - List all rules
GET    /api/admin/points/earning-rules/{ruleId}       - Get rule details
POST   /api/admin/points/earning-rules                - Create rule
PUT    /api/admin/points/earning-rules/{ruleId}       - Update rule
DELETE /api/admin/points/earning-rules/{ruleId}       - Delete rule
```

### Rule Operations
```
GET    /api/admin/points/earning-rules/by-action/{action} - Get by action type
GET    /api/admin/points/earning-rules/actions            - List available actions
PUT    /api/admin/points/earning-rules/{ruleId}/toggle-active - Toggle active status
POST   /api/admin/points/earning-rules/bulk-update-points    - Bulk update points
GET    /api/admin/points/earning-rules/statistics           - Rule statistics
```

---

## Available Action Types for Earning Rules

```
VISIT         - Points earned per restaurant visit
PURCHASE      - Points earned per dollar spent
REFERRAL      - Points earned for successful referral
SIGN_UP       - Bonus points for account creation
BIRTHDAY      - Bonus points on birthday
REVIEW        - Points for writing a review
SOCIAL_SHARE  - Points for sharing on social media
```

---

## Request/Response Format

### Standard Request Headers
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
Accept: application/json
```

### Standard Response Format
```json
{
  "success": true/false,
  "message": "Human readable message",
  "data": {
    // Response data object
  },
  "timestamp": "2026-09-16T14:30:00Z"
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error description",
  "data": null,
  "timestamp": "2026-09-16T14:30:00Z"
}
```

---

## Query Parameters by Endpoint

### Pagination (Most List Endpoints)
```
page=0        - Page number (0-indexed)
size=20       - Items per page (default 20)
```

### Offer Filtering
```
locationId=1  - Filter by restaurant location
status=ACTIVE - Filter by status (ACTIVE, INACTIVE, DRAFT)
category=Food - Filter by category
fromDate=...  - Filter from date
toDate=...    - Filter to date
```

### Reward Item Filtering
```
restaurantId=1 - Filter by restaurant
category=Food   - Filter by category
available=true  - Filter by availability (true/false)
```

### Points Filtering
```
restaurantId=1 - Filter by restaurant
userId=123     - Filter by user
action=VISIT   - Filter by action type
```

---

## HTTP Status Codes

```
200 OK                      - Request successful
201 Created                 - Resource created successfully
204 No Content              - Successful request with no content
400 Bad Request             - Invalid request/validation error
401 Unauthorized            - Missing or invalid token
403 Forbidden               - Insufficient permissions
404 Not Found               - Resource not found
409 Conflict                - Resource already exists
422 Unprocessable Entity    - Validation error
500 Internal Server Error   - Server error
503 Service Unavailable     - Server maintenance
```

---

## Field Requirements by Resource

### Offer Request (10 Required Fields)
```
name              (string, required)      - Offer title
locationId        (long, required)        - Restaurant location ID
startDate         (date, required)        - Start date (YYYY-MM-DD)
endDate           (date, required)        - End date (must be >= startDate)
status            (string, required)      - ACTIVE, INACTIVE, or DRAFT
discountValue     (decimal, required)     - Discount amount (must be > 0)
discountLabel     (string, required)      - Display text ("20% off", "$5 off")
description       (string, required)      - Offer description
photoUrl          (string, optional)      - Image URL
perUserLimit      (integer, required)     - Max uses per guest (min 1)
```

### Reward Item Request
```
name              (string, required)
pointsRequired    (integer, required)
description       (string, required)
restaurantId      (long, required)
category          (string, required)
image             (string, optional)
available         (boolean, default: true)
inventory         (integer, optional)
```

### Points Credit Request
```
userId            (long, required)
amount            (long, required)       - Points to credit (positive number)
reason            (string, required)     - Reason for crediting
```

### Points Debit Request
```
userId            (long, required)
amount            (long, required)       - Points to debit (positive number)
reason            (string, required)     - Reason for debiting
```

---

## Key Response Fields

### Offer Response
```
id, name, locationId, locationName, startDate, endDate, status,
discountValue, discountLabel, description, photoUrl, perUserLimit,
redemptions, createdAt, updatedAt
```

### Redemption Response
```
redemptionId, redemptionCode, status, offerName, offerDescription,
userId, userEmail, mobileNumber, offerId, discountType, discountValue,
discountLabel, confirmedAt, codeExpiresAt
```

### Reward Item Response
```
id, name, pointsRequired, description, restaurantId, category, image,
available, inventory, createdAt, updatedAt
```

### Reward Tier Response
```
id, name, minPoints, maxPoints, benefits, icon, createdAt
```

### Guest Rewards Profile Response
```
userId, currentPoints, currentTier, nextTier, availableRewards,
pointsEarningRate, memberSince
```

### Points Response
```
userId, balance (or pointsAdded/Deducted/Reversed), newBalance
```

---

## Common Workflow Examples

### Complete Offer Redemption Workflow
1. `GET /api/offers?locationId=1` - Browse offers
2. `GET /api/offers/{offerId}` - View details
3. `POST /api/offers/{offerId}/redeem` - Get code (guest)
4. `POST /api/offers/redeem/{code}/confirm` - Validate code (staff)

### Complete Reward Redemption Workflow
1. `GET /api/rewards/profile` - View profile & eligibility
2. `POST /api/rewards/{rewardId}/redeem` - Redeem reward
3. Staff uses returned code at point-of-sale

### Points Award Workflow
1. Guest makes purchase at restaurant
2. `POST /api/admin/points/credit` - Staff/admin awards points
3. Guest balance updated immediately

---

## Troubleshooting Common Issues

### 401 Unauthorized
**Cause**: Missing or expired token
**Solution**: Login again to get fresh token, or refresh token

### 403 Forbidden
**Cause**: Insufficient role permissions
**Solution**: Check user role (ADMIN, STAFF, GUEST)

### 400 Bad Request - Validation Error
**Cause**: Invalid request data
**Solution**: Check field requirements, data types, and ranges

### 409 Conflict - Duplicate
**Cause**: Resource already exists
**Solution**: Check unique constraints (email, codes, etc.)

### 422 Unprocessable - Date Invalid
**Cause**: End date before start date
**Solution**: Ensure endDate >= startDate

### Insufficient Points
**Cause**: Guest doesn't have enough points
**Solution**: Show remaining points needed, suggest ways to earn

---

## Performance Tips

### Caching Recommendations
- Cache offers list: 5 minutes
- Cache rewards profile: 2 minutes
- Cache tiers/items: 1 hour
- Cache user balance: 1 minute

### Pagination
- Default page size: 20 items
- Max page size: 100 items
- Recommend lazy loading for lists

### Batch Operations
- Use bulk endpoints for 5+ items
- Reduce API calls by 80%

---

## Security Notes

1. **Never** store passwords in frontend
2. **Never** log sensitive tokens/keys
3. **Always** use HTTPS in production
4. **Always** validate input before sending
5. **Always** check success flag in response
6. **Always** implement token expiry handling
7. Use httpOnly cookies for token storage (recommended)
8. Implement CSRF protection for state-changing operations

---

**Last Updated**: 2026-09-16
**API Version**: 1.0.0
**Status**: Production Ready ✅
