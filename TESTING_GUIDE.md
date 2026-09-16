# Quick Testing & Integration Guide

## Database Setup

```bash
# Run migrations
cd /Users/dineshkumar/Downloads/backend
./mvnw flyway:migrate

# Verify migration created tables
psql -U your_user -d your_db -c "\dt" | grep -E "(offer|redemption|reward|receipt|tier)"
```

## Configuration

Add to `application.properties` or `application.yml`:

```properties
# Offer redemption settings
offer.code.validity.minutes=60
offer.code.auto-expire.enabled=true

# Receipt claiming settings
upload.receipts.path=/var/uploads/receipts
receipt.points=15
receipt.auto-approve=false
receipt.duplicate-check-hours=24

# Points settings
points.earning.enabled=true
```

## Testing Endpoints

### Phase 2: Guest Offers API

```bash
# 1. List active offers (with optional category filter)
curl -X GET "http://localhost:8080/api/offers?locationId=1&page=0&size=20"
curl -X GET "http://localhost:8080/api/offers?locationId=1&category=PERCENT&page=0&size=20"

# 2. Get offer details
curl -X GET "http://localhost:8080/api/offers/1"

# 3. Redeem offer (returns 6-digit code)
curl -X POST "http://localhost:8080/api/offers/1/redeem" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"

# 4. Validate code at POS (staff only)
curl -X POST "http://localhost:8080/api/offers/redeem/514527/confirm" \
  -H "Authorization: Bearer STAFF_JWT_TOKEN"
```

### Phase 4: Guest Rewards API

```bash
# 1. Get complete rewards profile
curl -X GET "http://localhost:8080/api/rewards/profile?restaurantId=1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 2. Redeem a reward item (returns 6-digit code)
curl -X POST "http://localhost:8080/api/rewards/123/redeem" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"rewardItemId":123,"restaurantId":1}'

# 3. List all reward tiers
curl -X GET "http://localhost:8080/api/rewards/tiers"
```

### Phase 5: Receipt Scanning

```bash
# 1. Upload receipt image and claim points
curl -X POST "http://localhost:8080/api/rewards/receipt/claim" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@receipt.jpg" \
  -F "restaurantId=1" \
  -F "receiptAmount=45.50" \
  -F "receiptDate=2026-09-16"

# 2. Admin: Get pending receipts (requires ADMIN role)
curl -X GET "http://localhost:8080/api/admin/receipts/pending?restaurantId=1&page=0&size=20" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"

# 3. Admin: Approve receipt
curl -X PUT "http://localhost:8080/api/admin/receipts/5/approve" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"

# 4. Admin: Reject receipt
curl -X PUT "http://localhost:8080/api/admin/receipts/5/reject" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"reason":"Receipt amount too low"}'
```

## Data Setup for Testing

### Create Test Offer (Admin)

```bash
curl -X POST "http://localhost:8080/api/admin/offers" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "23% Off Breakfast",
    "locationId": 1,
    "startDate": "2026-09-16",
    "endDate": "2026-12-31",
    "status": "ACTIVE",
    "discountType": "PERCENT",
    "discountValue": 23,
    "discountLabel": "23% off",
    "description": "Save 23% on any breakfast item",
    "restrictions": ["Dine-in only", "Monday-Friday"],
    "photoUrl": "https://example.com/breakfast.jpg",
    "rating": 4.7,
    "ratingCount": 125,
    "category": "PERCENT",
    "perUserLimit": 2,
    "perUserDailyLimit": 1,
    "inventory": 50,
    "originalPrice": 12.99
  }'
```

### Create Test Reward Tier (Admin)

```bash
curl -X POST "http://localhost:8080/api/admin/reward-tiers" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Gold",
    "restaurantId": 1,
    "pointsThreshold": 350,
    "tierOrder": 2,
    "perks": ["Free dessert", "Priority seating"],
    "color": "gold"
  }'
```

### Create Test Reward Item (Admin)

```bash
curl -X POST "http://localhost:8080/api/admin/reward-items" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Free Coffee",
    "description": "Any size, any blend",
    "pointsCost": 100,
    "restaurantId": 1,
    "icon": "coffee",
    "category": "beverage",
    "available": true
  }'
```

### Create Points Earning Rule (Admin)

```bash
curl -X POST "http://localhost:8080/api/admin/earning-rules" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "action": "dine_in",
    "pointsValue": 15,
    "restaurantId": 1,
    "description": "Claim points when you dine without joining waitlist",
    "clickable": true,
    "actionUrl": "/rewards/receipt/upload"
  }'
```

## Database Query Examples

### Check Offer Details
```sql
SELECT 
  id, name, discount_type, discount_label, category, 
  per_user_limit, inventory, status
FROM offers 
WHERE restaurant_id = 1 AND status = 'ACTIVE';
```

### Check Redemption Codes
```sql
SELECT 
  redemption_code, user_id, status, code_expires_at, created_at
FROM redemptions 
WHERE offer_id = 1
ORDER BY created_at DESC;
```

### Check User Points & Tier
```sql
SELECT 
  dp.user_id, dp.balance, rt.name as tier
FROM dinerly_points dp
LEFT JOIN reward_tiers rt ON dp.balance >= rt.points_threshold
WHERE dp.user_id = 123;
```

### Check Receipt Claims
```sql
SELECT 
  id, user_id, status, points_claimed, created_at, approved_at
FROM receipt_claims 
WHERE restaurant_id = 1 AND status != 'DUPLICATE'
ORDER BY created_at DESC;
```

## Debugging Tips

### Check if code generation is working
```sql
SELECT COUNT(DISTINCT redemption_code) as unique_codes FROM redemptions;
SELECT COUNT(*) as total_redemptions FROM redemptions;
```

### Check for duplicate codes
```sql
SELECT redemption_code, COUNT(*) as count 
FROM redemptions 
GROUP BY redemption_code 
HAVING COUNT(*) > 1;
```

### Check expired codes
```sql
SELECT redemption_code, status, code_expires_at 
FROM redemptions 
WHERE code_expires_at < NOW() 
AND status = 'GENERATED';
```

### Check points ledger
```sql
SELECT user_id, reason, delta, balance_after 
FROM points_ledger 
WHERE user_id = 123 
ORDER BY created_at DESC 
LIMIT 20;
```

## Common Issues & Solutions

### Issue: "Redemption code not found"
- Verify code exists: `SELECT redemption_code FROM redemptions;`
- Check expiry time: `SELECT * FROM redemptions WHERE redemption_code = '514527';`
- Code might have expired (past code_expires_at timestamp)

### Issue: "Insufficient points for reward"
- Check balance: `SELECT balance FROM dinerly_points WHERE user_id = 123;`
- Check reward cost: `SELECT points_cost FROM reward_items WHERE id = 123;`
- Ensure points were credited (check points_ledger)

### Issue: "Duplicate receipt detected"
- Check recent claims: `SELECT * FROM receipt_claims WHERE user_id = 123 AND created_at > NOW() - INTERVAL '1 day';`
- Modify duplicate detection window in config

### Issue: File upload fails
- Verify directory exists: `ls -la /var/uploads/receipts/`
- Check permissions: `chmod 755 /var/uploads/receipts`
- Verify file is JPEG/PNG: `file receipt.jpg`

## Performance Testing

### Load test: 1000 concurrent offer redemptions
```bash
# Using Apache Bench
ab -n 1000 -c 100 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  "http://localhost:8080/api/offers/1/redeem"

# Check for duplicate codes
SELECT COUNT(*) as total, COUNT(DISTINCT redemption_code) as unique 
FROM redemptions;
```

### Load test: Concurrent points deduction
```bash
# Multiple reward redemptions
for i in {1..100}; do
  curl -X POST "http://localhost:8080/api/rewards/123/redeem" \
    -H "Authorization: Bearer USER_JWT_TOKEN_$i" \
    -H "Content-Type: application/json" \
    -d '{"rewardItemId":123,"restaurantId":1}' &
done
wait
```

## Swagger API Documentation

After starting the server, view comprehensive API docs at:
```
http://localhost:8080/swagger-ui/index.html
```

All 7 new guest endpoints are documented with:
- Request/response schemas
- Parameter descriptions
- Status codes
- Example requests

---

**Note:** Replace `YOUR_JWT_TOKEN`, `ADMIN_JWT_TOKEN`, and IDs with actual values from your testing environment.
