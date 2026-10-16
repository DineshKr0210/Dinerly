# QR Code Offers - Quick Testing Guide

**Status:** ✅ Ready for Testing  
**Date:** October 16, 2026

---

## Quick Start

### Prerequisites
- Backend running on `http://localhost:8080`
- Valid JWT token with authenticated user
- Restaurant ID (usually `1`)
- Offer ID to redeem

### Environment Variables
```bash
export BASE_URL="http://localhost:8080"
export TOKEN="your_jwt_token_here"
export RESTAURANT_ID="1"
export OFFER_ID="123"
```

---

## Test 1: Redeem Offer and Get QR Code

**Endpoint:** `POST /api/offers/{offerId}/redeem`

**Command:**
```bash
curl -X POST $BASE_URL/api/offers/$OFFER_ID/redeem \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

**Expected Response:**
```json
{
  "success": true,
  "data": {
    "redemptionId": 1001,
    "redemptionCode": "456789",
    "expiresAt": "2026-10-16T15:30:00",
    "message": "Show this code or QR to your server",
    "offerName": "Offer Name Here",
    "offerDescription": "Offer Description",
    "qrCodeDataUri": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAASwAAAEsCA..."
  }
}
```

**Verify:**
- ✅ Response includes `qrCodeDataUri`
- ✅ `qrCodeDataUri` starts with `data:image/png;base64,`
- ✅ `redemptionCode` is 6 digits (e.g., "456789")
- ✅ `expiresAt` is 1 hour from now

**Save for Next Tests:**
```bash
export REDEMPTION_CODE="456789"
export QR_CODE_DATA_URI="data:image/png;base64,iVBORw0KGGoA..."
```

---

## Test 2: Validate by Manual Code Entry

**Endpoint:** `POST /api/offers/validate` (NEW)

**Request - Manual Code:**
```bash
curl -X POST $BASE_URL/api/offers/validate \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -G --data-urlencode "restaurantId=$RESTAURANT_ID" \
  -d '{
    "redemptionCode": "'$REDEMPTION_CODE'"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Offer validated and redeemed successfully",
  "data": {
    "redemptionId": 1001,
    "redemptionCode": "456789",
    "status": "COMPLETED",
    "offerName": "Offer Name",
    "discountValue": 5.00,
    "confirmedAt": "2026-10-16T14:30:00",
    "qrCodeDataUri": "data:image/png;base64,..."
  }
}
```

**Verify:**
- ✅ status = "COMPLETED"
- ✅ discountValue is correct
- ✅ confirmedAt is present
- ✅ Response includes offer details

---

## Test 3: Simulate QR Scan (Manual)

**How QR Scanners Work:**
- User points camera at QR code
- QR scanner app decodes the image
- Returns the **plain text** encoded in QR
- In this case: the 6-digit redemption code

**For Testing:**
Extract the 6-digit code from QR and use in Test 4

**Request - QR Code Scan:**
```bash
# Simulate QR scanner returning decoded text "456789"
curl -X POST $BASE_URL/api/offers/validate \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -G --data-urlencode "restaurantId=$RESTAURANT_ID" \
  -d '{
    "scannedQRText": "456789"
  }'
```

**Expected Response:**
Same as Test 2 (should work identically)

**Verify:**
- ✅ Both manual code and QR scan produce same result
- ✅ status = "COMPLETED"

---

## Test 4: Invalid Code Format

**Endpoint:** `POST /api/offers/validate`

**Request - Too Few Digits:**
```bash
curl -X POST $BASE_URL/api/offers/validate \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -G --data-urlencode "restaurantId=$RESTAURANT_ID" \
  -d '{
    "redemptionCode": "12345"
  }'
```

**Expected Response:**
```json
{
  "success": false,
  "message": "Invalid code format. Please enter a 6-digit code or scan the QR code"
}
```

**Verify:**
- ✅ Error message is clear
- ✅ HTTP 400 status

**Also Test:**
- Non-numeric: `"ABCDEF"` → should error
- Non-existent code: `"000000"` → should error ("Invalid or expired")
- Expired code: If 1+ hour old → should error

---

## Test 5: Verify Database Storage

**Check Redemption Record:**
```sql
-- Connect to database
psql -h localhost -d restaurant_db -U postgres

-- See the redemption we just created
SELECT id, redemption_code, offer_id, campaign_id, qr_code_data, status 
FROM redemptions 
WHERE redemption_code = '456789';
```

**Expected:**
```
 id  | redemption_code | offer_id | campaign_id | qr_code_data | status
-----+-----------------+----------+-------------+--------------+-----------
1001 | 456789          | 123      |        NULL | \x89PNG...   | COMPLETED
```

**Verify:**
- ✅ `redemption_code` is correct
- ✅ `offer_id` is NOT NULL (Offer type)
- ✅ `campaign_id` is NULL (not a campaign)
- ✅ `qr_code_data` is NOT NULL (binary PNG bytes)
- ✅ `status` is COMPLETED

---

## Test 6: Campaign Code Has NO QR

**Redeem a Campaign Code:**
```bash
curl -X POST $BASE_URL/api/campaign/redeem/MY_CAMPAIGN_CODE \
  -H "Authorization: Bearer $TOKEN"
```

**Check Database:**
```sql
SELECT id, redemption_code, campaign_id, qr_code_data 
FROM redemptions 
WHERE campaign_id IS NOT NULL
LIMIT 1;
```

**Expected:**
```
 id  | redemption_code | campaign_id | qr_code_data
-----+-----------------+-------------+--------------
1002 | MY_CAMPAIGN_CODE|       456   | NULL
```

**Verify:**
- ✅ `qr_code_data` is NULL (no QR for campaigns)
- ✅ Campaign codes work as before (no change)

---

## Test 7: Reward Redemption Has NO QR

**Redeem Reward Points:**
```bash
curl -X POST $BASE_URL/api/rewards/redeem \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"pointsToRedeem": 100}'
```

**Check Database:**
```sql
SELECT id, reward_item_id, qr_code_data 
FROM redemptions 
WHERE reward_item_id IS NOT NULL
LIMIT 1;
```

**Expected:**
```
 id  | reward_item_id | qr_code_data
-----+----------------+--------------
1003 | 789            | NULL
```

**Verify:**
- ✅ `qr_code_data` is NULL (no QR for rewards)
- ✅ Rewards work as before (no change)

---

## Test 8: Code Expiry

**Setup:**
1. Redeem offer at 14:00
2. Code expires at 15:00 (1 hour)

**Test at 14:45 (Still Valid):**
```bash
curl -X POST $BASE_URL/api/offers/validate \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"redemptionCode": "456789"}' \
  -G --data-urlencode "restaurantId=1"
```

**Expected:** ✅ COMPLETED (success)

**Test at 15:01 (Expired):**
```bash
curl -X POST $BASE_URL/api/offers/validate \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"redemptionCode": "456789"}' \
  -G --data-urlencode "restaurantId=1"
```

**Expected:** ❌ Error - "Invalid or expired redemption code"

---

## Test 9: Cross-Restaurant Validation

**Setup:**
1. Redeem offer at Restaurant 1
2. Try to validate at Restaurant 2

**Generate Code at Restaurant 1:**
```bash
curl -X POST $BASE_URL/api/offers/123/redeem \
  -H "Authorization: Bearer $TOKEN" \
  -G --data-urlencode "restaurantId=1"
# Returns: redemptionCode = "456789"
```

**Validate at Restaurant 2:**
```bash
curl -X POST $BASE_URL/api/offers/validate \
  -H "Authorization: Bearer $TOKEN" \
  -G --data-urlencode "restaurantId=2" \
  -d '{"redemptionCode": "456789"}'
```

**Expected:** ❌ Error - "Invalid or expired redemption code"

**Verify:**
- ✅ Code cannot be used at different restaurant
- ✅ Security: Prevents code sharing across locations

---

## Test 10: Display QR Code in Frontend

**HTML Test:**
```html
<!-- Extract qrCodeDataUri from API response -->
<img 
  id="qrCode" 
  src="data:image/png;base64,iVBORw0KGgoAAAA..." 
  alt="QR Code"
  width="300"
  height="300"
>

<p>Code: <strong>456789</strong></p>
```

**JavaScript Test:**
```javascript
const qrCodeDataUri = "data:image/png;base64,iVBORw0KGGoA...";

// Verify it's a valid image
const img = new Image();
img.onload = () => {
  console.log("✅ QR code is valid and displayable");
  document.getElementById('qrCode').src = qrCodeDataUri;
};
img.onerror = () => {
  console.log("❌ QR code data is invalid");
};
img.src = qrCodeDataUri;
```

**Expected:**
- ✅ Image displays correctly
- ✅ QR code is readable and scannable

---

## Testing Checklist

### Redemption & QR Generation
- [ ] Test 1: Offer redemption returns QR code
- [ ] Verify QR code is valid Base64
- [ ] Verify 6-digit code format
- [ ] Verify expiry time is 1 hour

### Validation
- [ ] Test 2: Manual code validation works
- [ ] Test 3: QR scan validation works
- [ ] Test 4: Invalid code rejected with clear error
- [ ] Test 8: Expired code rejected

### Database
- [ ] Test 5: QR code stored in database
- [ ] Test 6: Campaign codes have NO QR
- [ ] Test 7: Reward redemptions have NO QR

### Security
- [ ] Test 9: Cross-restaurant code rejected
- [ ] Code cannot be redeemed twice
- [ ] Code cannot be used before generation

### Frontend
- [ ] Test 10: QR code displays properly
- [ ] Can scan with QR reader app
- [ ] Manual entry works as backup

---

## Common Issues & Fixes

### Issue: QR Code Not Generating

**Error:** Response doesn't include `qrCodeDataUri`

**Solution:** 
1. Check ZXing library is in pom.xml
2. Check QRCodeGenerator utility exists
3. Run migration V11 to add column

```bash
# Verify dependency
grep -r "zxing" pom.xml
# Should return: core and javase versions

# Verify column exists
SELECT column_name FROM information_schema.columns 
WHERE table_name='redemptions' AND column_name='qr_code_data';
```

### Issue: "Invalid code format"

**Error:** When validating "456789"

**Solution:** Code must be exactly 6 digits
```bash
# This works:
curl ... -d '{"redemptionCode": "456789"}'

# This fails:
curl ... -d '{"redemptionCode": " 456789"}' # leading space
curl ... -d '{"redemptionCode": "12345"}'   # only 5 digits
curl ... -d '{"redemptionCode": "45678A"}'  # contains letters
```

### Issue: Old Validation Endpoint Still Works

**OK:** Both endpoints work:
- Old: `POST /api/offers/redeem/{code}/confirm` (manual code only)
- New: `POST /api/offers/validate` (QR + code)

**Use new endpoint for QR support, old still works for compatibility**

---

## Quick Reference

| Action | Endpoint | Method | Auth |
|--------|----------|--------|------|
| Redeem offer | `/api/offers/{id}/redeem` | POST | ✅ User |
| Validate (NEW) | `/api/offers/validate` | POST | ✅ Staff |
| Validate (OLD) | `/api/offers/redeem/{code}/confirm` | POST | ✅ Staff |

| Field | Value | Notes |
|-------|-------|-------|
| Code Format | 6 digits | "456789" |
| QR Expires | 1 hour | From redemption time |
| QR Size | 1-5 KB | Per PNG image |
| Storage | Database BYTEA | Compressed PNG data |

---

**All tests should PASS before deploying to production!**

Questions? See `OFFERS_QR_CODE_IMPLEMENTATION.md` for detailed guide.

