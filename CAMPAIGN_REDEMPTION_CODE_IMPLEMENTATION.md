# Campaign Redemption Code Implementation Guide

## Overview
This implementation enables optional redemption codes for marketing campaigns. Codes are generated at **PUBLISH time** (not create time) to ensure fresh TTL, and are **optional** based on campaign type.

---

## Database Changes

### 1. SQL Migration: V3__add_campaign_redemption_codes.sql

```sql
-- Add has_redemption_code flag to campaigns table
ALTER TABLE campaigns
    ADD COLUMN has_redemption_code BOOLEAN NOT NULL DEFAULT false;

-- Create index for faster campaign code lookups
CREATE INDEX idx_campaigns_has_redemption_code ON campaigns(has_redemption_code);

-- Create index for campaign-based redemption queries
CREATE INDEX idx_redemptions_campaign_status ON redemptions(campaign_id, status);

-- Create index for code expiry queries
CREATE INDEX idx_redemptions_code_expires_at ON redemptions(code_expires_at);
```

**What it does:**
- Adds `has_redemption_code` flag to campaigns (TRUE = generate codes, FALSE = no codes)
- Creates performance indexes for quick lookups of campaign codes

---

## API Endpoints

### Create Campaign (with optional code generation flag)

```http
POST /api/admin/campaigns
Content-Type: application/json

{
  "name": "Weekend Pasta Special",
  "message": "20% off all pasta dishes",
  "channel": "SMS",
  "audience": "RECENT_30D",
  "restaurantId": 1,
  "scheduledAt": "2026-09-25T10:00:00",
  "endDate": "2026-09-27T23:59:59",
  "hasRedemptionCode": true,
  "templateId": null
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Weekend Pasta Special",
    "hasRedemptionCode": true,
    "codesGenerated": 0,
    "status": "SCHEDULED",
    "reach": 0,
    "redemptions": 0
  }
}
```

### Publish Campaign (generates codes if enabled)

```http
POST /api/admin/campaigns/1/publish?immediate=true
```

**Behind the scenes:**
1. Fetch campaign with ID 1
2. Get audience recipients (phone numbers based on segment)
3. **IF hasRedemptionCode = true:**
   - Loop through each recipient phone number
   - Generate unique 6-digit code (e.g., "514527")
   - Create Redemption record with:
     - `campaign_id` = campaign ID
     - `redemption_code` = unique code
     - `guest_phone` = recipient phone
     - `status` = GENERATED
     - `code_expires_at` = now + 24 hours
     - `restaurant_id` = campaign restaurant ID
   - Store code-to-phone mapping in memory
4. **Send SMS to each recipient:**
   - If has codes: `"20% off all pasta! Use code: 514527"`
   - If no codes: `"20% off all pasta!"`
5. Update campaign: `status = ACTIVE`, `reach = N`, `sent_count = N`

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Weekend Pasta Special",
    "status": "ACTIVE",
    "reach": 150,
    "sentCount": 150,
    "codesGenerated": 150,
    "hasRedemptionCode": true,
    "redemptions": 0
  }
}
```

### Validate Campaign Code (at POS when guest enters code)

```http
POST /api/admin/redemptions/validate-campaign-code
Content-Type: application/json

{
  "code": "514527",
  "campaignId": 1
}
```

**Backend logic:**
1. Query: `findValidCampaignCode(code="514527", campaignId=1)`
   - SQL: `SELECT r FROM Redemption r WHERE r.redemption_code = '514527' 
           AND r.campaign.id = 1 AND r.status = 'GENERATED' 
           AND r.code_expires_at > CURRENT_TIMESTAMP`
2. If found:
   - Verify code exists, status is GENERATED, not expired
   - Update redemption: `status = COMPLETED`, `redeemed_at = NOW()`
   - Return success with campaign/guest details
3. If not found:
   - Return error: "Invalid or expired campaign code"

**Success Response:**
```json
{
  "success": true,
  "message": "Campaign code validated and completed",
  "data": {
    "code": "514527",
    "campaignId": 1,
    "redemptionId": 42,
    "guestPhone": "+1-647-123-4567",
    "status": "COMPLETED"
  }
}
```

**Error Response:**
```json
{
  "success": false,
  "message": "Invalid or expired campaign code",
  "error": "Code not found for this campaign or has expired"
}
```

---

## Database Queries for Code Tracking

### Query 1: Count codes generated for a campaign

```sql
SELECT COUNT(*) as codes_generated
FROM redemptions
WHERE campaign_id = 1 AND status = 'GENERATED';
-- Result: 150
```

### Query 2: Count codes actually redeemed from a campaign

```sql
SELECT COUNT(*) as codes_redeemed
FROM redemptions
WHERE campaign_id = 1 AND status = 'COMPLETED';
-- Result: 87
```

### Query 3: Track redemption rate

```sql
SELECT 
    campaign_id,
    COUNT(CASE WHEN status = 'GENERATED' THEN 1 END) as codes_generated,
    COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as codes_redeemed,
    ROUND(100.0 * COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) / 
          NULLIF(COUNT(CASE WHEN status = 'GENERATED' THEN 1 END), 0), 2) as redemption_rate_pct
FROM redemptions
WHERE campaign_id = 1
GROUP BY campaign_id;

-- Result:
-- campaign_id | codes_generated | codes_redeemed | redemption_rate_pct
-- 1           | 150             | 87             | 58.00
```

### Query 4: Find all unredeemed codes for a campaign

```sql
SELECT 
    redemption_code,
    guest_phone,
    code_expires_at,
    CASE 
        WHEN code_expires_at < CURRENT_TIMESTAMP THEN 'EXPIRED'
        ELSE 'ACTIVE'
    END as code_status
FROM redemptions
WHERE campaign_id = 1 AND status = 'GENERATED'
ORDER BY code_expires_at ASC;
```

### Query 5: Expired codes that were never redeemed

```sql
SELECT COUNT(*) as expired_unredeemed
FROM redemptions
WHERE campaign_id = 1 
  AND status = 'GENERATED' 
  AND code_expires_at < CURRENT_TIMESTAMP;
-- Result: 10
```

### Query 6: Map campaign to all redemptions

```sql
SELECT 
    r.id,
    r.redemption_code,
    r.guest_phone,
    r.status,
    r.redeemed_at,
    c.name as campaign_name,
    c.message as campaign_message
FROM redemptions r
LEFT JOIN campaigns c ON r.campaign_id = c.id
WHERE r.campaign_id = 1
ORDER BY r.created_at DESC;
```

---

## Redemption Code Flow Diagram

### Scenario 1: Offer Campaign (WITH Codes)

```
ADMIN CREATES CAMPAIGN
  ├─ Name: "20% off pasta"
  ├─ Message: "20% off all pasta dishes"
  ├─ Audience: RECENT_30D (150 recipients)
  └─ hasRedemptionCode: TRUE ✅
       ↓
ADMIN PUBLISHES CAMPAIGN
  ├─ Get 150 phone numbers
  ├─ Generate 150 unique codes:
  │   ├─ Code 1: "514527" → Phone 1
  │   ├─ Code 2: "928301" → Phone 2
  │   └─ Code 150: "772845" → Phone 150
  │
  ├─ Create 150 Redemption rows:
  │   DB INSERT INTO redemptions (
  │     campaign_id=1,
  │     redemption_code='514527',
  │     guest_phone='+1-647-xxx',
  │     status='GENERATED',
  │     code_expires_at=NOW()+24h,
  │     restaurant_id=1
  │   )
  │
  └─ Send 150 SMS messages:
      "20% off all pasta! Use code: 514527"
           ↓
GUEST RECEIVES SMS
  "20% off all pasta! Use code: 514527"
       ↓
GUEST GOES TO RESTAURANT & ENTERS CODE AT POS
       ↓
POS VALIDATES CODE
  POST /api/admin/redemptions/validate-campaign-code
  {
    "code": "514527",
    "campaignId": 1
  }
       ↓
BACKEND VALIDATION:
  Query: findValidCampaignCode("514527", 1)
  ├─ Find Redemption where:
  │   - redemption_code = '514527'
  │   - campaign_id = 1
  │   - status = 'GENERATED'
  │   - code_expires_at > NOW()
  │
  ├─ Result Found! ✅
  │   - Update: status = 'COMPLETED', redeemed_at = NOW()
  │   - Return: { success: true, redemptionId: 42, guestPhone: '+1-647-xxx' }
  │
  └─ Or Not Found ❌
      - Return: { success: false, message: 'Invalid or expired code' }
       ↓
POS SHOWS RESULT TO STAFF
  ✅ "Code valid! Guest qualifies for 20% off"
  or
  ❌ "Invalid or expired code. Please check."
```

### Scenario 2: General Marketing Campaign (NO Codes)

```
ADMIN CREATES CAMPAIGN
  ├─ Name: "Check our new menu!"
  ├─ Message: "Discover our summer menu with fresh items"
  ├─ Audience: ALL (500 recipients)
  └─ hasRedemptionCode: FALSE ❌
       ↓
ADMIN PUBLISHES CAMPAIGN
  ├─ Get 500 phone numbers
  ├─ NO code generation (hasRedemptionCode = false)
  │
  └─ Send 500 SMS messages:
      "Discover our summer menu with fresh items"
           ↓
GUEST RECEIVES SMS
  "Discover our summer menu with fresh items"
       ↓
GUEST READS SMS - NO CODE ENTRY NEEDED ✅
```

---

## Campaign Types & Code Usage

| Campaign Type | hasRedemptionCode | Example | Code Needed? |
|---------------|-------------------|---------|--------------|
| Offer/Coupon | TRUE | "20% off pasta - CODE123" | ✅ YES |
| Loyalty Reward | TRUE | "Free coffee - CODE456" | ✅ YES |
| Menu Announcement | FALSE | "Check our new summer menu!" | ❌ NO |
| Grand Opening | FALSE | "Grand opening at new location!" | ❌ NO |
| Loyalty Tier Notice | FALSE | "You've reached Platinum tier!" | ❌ NO |
| Survey/Review Request | FALSE | "Leave a review, earn 50 points" | ❌ NO |

---

## Testing Checklist

### Unit Tests

- [ ] **Code Generation**: 6-digit codes are unique (no duplicates)
- [ ] **Code Format**: Codes match pattern `[0-9]{6}`
- [ ] **Code Uniqueness**: `existsByRedemptionCode()` returns false for new codes
- [ ] **TTL Calculation**: `codeExpiresAt = now + 24 hours`
- [ ] **Conditional Logic**: Codes only generated when `hasRedemptionCode = true`

### Integration Tests

- [ ] **Campaign Creation**: `hasRedemptionCode` field persists correctly
- [ ] **Campaign Publish**: Codes created for all recipients when flag is true
- [ ] **Code Validation**: `findValidCampaignCode()` returns valid codes
- [ ] **Code Completion**: Status changes from GENERATED to COMPLETED
- [ ] **Expiry Validation**: Expired codes are rejected
- [ ] **Campaign Mapping**: Redeemed codes correctly mapped to campaign via `campaign_id` FK

### API Tests

```bash
# 1. Create campaign with codes enabled
curl -X POST http://localhost:8080/api/admin/campaigns \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Pasta Campaign",
    "message": "20% off pasta",
    "channel": "SMS",
    "audience": "RECENT_30D",
    "restaurantId": 1,
    "hasRedemptionCode": true
  }'

# 2. Publish campaign (generates codes)
curl -X POST http://localhost:8080/api/admin/campaigns/1/publish?immediate=true

# 3. Validate code at POS
curl -X POST http://localhost:8080/api/admin/redemptions/validate-campaign-code \
  -H "Content-Type: application/json" \
  -d '{
    "code": "514527",
    "campaignId": 1
  }'

# 4. Check campaign redemption stats
curl -X GET http://localhost:8080/api/admin/campaigns/1

# Expected response:
# {
#   "codesGenerated": 150,
#   "redemptions": 87,
#   "hasRedemptionCode": true
# }
```

---

## Important Notes

### Code TTL (Time-To-Live)

- **Duration**: 24 hours from code generation
- **When**: Set at **PUBLISH time** (not create time)
- **Why**: Ensures guest has 24 hours from when campaign goes live to redeem
- **Query**: `WHERE code_expires_at > CURRENT_TIMESTAMP`

### Campaign Attribution

- **How**: Via `campaign_id` Foreign Key in redemptions table
- **Ensures**: Every code can be traced back to originating campaign
- **Benefit**: Accurate campaign performance tracking and ROI calculation

### Code Uniqueness

- **Method**: Check `redemptionRepository.existsByRedemptionCode(code)` before saving
- **Retry Logic**: Regenerate if code exists (up to 20 attempts)
- **Index**: `UNIQUE(redemption_code)` constraint on table

### Backwards Compatibility

- **Existing Campaigns**: Will have `hasRedemptionCode = false` (default)
- **Existing Offers**: Unaffected - offer redemptions still work as before
- **Migration**: No data loss or downtime required

---

## Performance Considerations

### Indexes Created

```sql
CREATE INDEX idx_campaigns_has_redemption_code ON campaigns(has_redemption_code);
CREATE INDEX idx_redemptions_campaign_status ON redemptions(campaign_id, status);
CREATE INDEX idx_redemptions_code_expires_at ON redemptions(code_expires_at);
```

### Query Optimization

- **Campaign lookup**: Uses `has_redemption_code` index for filtering
- **Code validation**: Uses `(campaign_id, status)` composite index
- **Expiry cleanup**: Uses `code_expires_at` index for batch expiry jobs

### Expected Performance

- **Code generation**: ~100 codes/second (sequential generation + DB save)
- **Code validation**: <50ms per query (indexed lookups)
- **Campaign publish**: ~5-10 seconds for 500 recipients + SMS sending

---

## Troubleshooting

### Issue: Codes not generating even with hasRedemptionCode=true

**Check:**
1. Campaign entity has `hasRedemptionCode` field set to `true`
2. Database migration V3 was applied
3. Column `has_redemption_code` exists in campaigns table

**Debug:**
```sql
SELECT has_redemption_code FROM campaigns WHERE id = 1;
-- Should return: true
```

### Issue: "Invalid or expired code" error on valid code

**Check:**
1. Code exists in redemptions table
2. Code status is 'GENERATED' (not COMPLETED)
3. `code_expires_at` is still in future
4. `campaign_id` matches the campaign ID provided

**Debug:**
```sql
SELECT id, redemption_code, status, code_expires_at 
FROM redemptions 
WHERE redemption_code = '514527' AND campaign_id = 1;
```

### Issue: Campaign shows codesGenerated = 0 after publish

**Check:**
1. Campaign has `hasRedemptionCode = true`
2. Recipients list is not empty (audience filtering worked)
3. No exceptions during code generation
4. Redemption records were saved to database

**Debug:**
```sql
SELECT COUNT(*) FROM redemptions WHERE campaign_id = 1 AND status = 'GENERATED';
```

---

## Future Enhancements

- [ ] Custom code format (e.g., "PASTA20" instead of "514527")
- [ ] Adjustable TTL per campaign type
- [ ] Bulk code generation without sending SMS
- [ ] Code redemption analytics dashboard
- [ ] Automatic code expiry cleanup job
- [ ] SMS template integration for personalized codes
