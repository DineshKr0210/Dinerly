# 📌 Admin Redemption API - After Cleanup Reference

## Final API Endpoints (10 Total)

### 🎯 Core CRUD
```
GET    /api/admin/redemptions
  Query params: locationId, status, from, to, page, size
  Purpose: List all redemptions with filtering
  Response: Page<RedemptionResponse>

GET    /api/admin/redemptions/{redemptionId}
  Path param: redemptionId
  Purpose: Get specific redemption details
  Response: RedemptionResponse
```

### 💼 Business Filtering
```
GET    /api/admin/redemptions/by-offer/{offerId}
  Path param: offerId
  Query params: page, size
  Purpose: View redemptions for specific offer (offer performance)
  Response: Page<RedemptionResponse>

GET    /api/admin/redemptions/by-user/{userId}
  Path param: userId
  Query params: page, size
  Purpose: View redemption history for specific user (customer support)
  Response: Page<RedemptionResponse>
```

### 📊 Analytics & Reporting
```
GET    /api/admin/redemptions/export
  Query params: locationId, status, from, to
  Purpose: Export redemptions as CSV
  Response: byte[] (CSV file)
  Content-Type: text/csv

GET    /api/admin/redemptions/statistics
  Query params: locationId, from, to
  Purpose: Get redemption statistics (counts, values)
  Response: Map<String, Object>
```

### ✅ Operations
```
PUT    /api/admin/redemptions/{redemptionId}/cancel
  Path param: redemptionId
  Query param: reason (optional)
  Purpose: Cancel/reverse a redemption
  Request Body: Empty
  Response: RedemptionResponse

POST   /api/admin/redemptions/expire-bulk
  Purpose: Bulk expire multiple redemptions
  Request Body: {
    "redemptionIds": [1, 2, 3],
    "reason": "expired" (optional)
  }
  Response: Map<String, Object>
    Example: {"expired": 3}
```

---

## 🗑️ Removed Endpoints (4)

```
❌ GET    /api/admin/redemptions/by-code/{code}
   REMOVED: Stub implementation

❌ GET    /api/admin/redemptions/by-status/{status}
   REMOVED: Redundant - use ?status query param on main GET instead

❌ GET    /api/admin/redemptions/expired-codes
   REMOVED: Incomplete filtering logic

❌ PUT    /api/admin/redemptions/{redemptionId}/expire
   REMOVED: Overlaps with /cancel endpoint
```

---

## 📋 Quick Usage Examples

### Get All Redemptions with Filters
```bash
GET /api/admin/redemptions?status=PENDING&locationId=1&page=0&size=20
```

### Get Specific Redemption
```bash
GET /api/admin/redemptions/42
```

### View Redemptions for an Offer
```bash
GET /api/admin/redemptions/by-offer/7?page=0&size=20
```

### View User's Redemption History
```bash
GET /api/admin/redemptions/by-user/15?page=0&size=20
```

### Export Redemptions as CSV
```bash
GET /api/admin/redemptions/export?locationId=1&status=COMPLETED
```

### Get Statistics
```bash
GET /api/admin/redemptions/statistics?locationId=1
```

### Cancel a Redemption
```bash
PUT /api/admin/redemptions/42/cancel?reason=fraudulent
```

### Bulk Expire Redemptions
```bash
POST /api/admin/redemptions/expire-bulk
Content-Type: application/json

{
  "redemptionIds": [1, 2, 3],
  "reason": "offer_expired"
}
```

---

## 🔑 Key Points

- **All filtering through query parameters** - No redundant path-based endpoints
- **Cancel endpoint handles all reversal scenarios** - Including expiration
- **Status can be filtered from main list** - No separate by-status endpoint
- **Bulk operations for efficiency** - Expire multiple codes at once
- **Analytics built-in** - Statistics and export capabilities

---

## ✅ Endpoints Summary Table

| Endpoint | HTTP | Purpose | Status |
|----------|------|---------|--------|
| `/api/admin/redemptions` | GET | List with filters | ✅ ACTIVE |
| `/api/admin/redemptions/{id}` | GET | Get detail | ✅ ACTIVE |
| `/api/admin/redemptions/by-offer/{id}` | GET | Filter by offer | ✅ ACTIVE |
| `/api/admin/redemptions/by-user/{id}` | GET | Filter by user | ✅ ACTIVE |
| `/api/admin/redemptions/export` | GET | Export CSV | ✅ ACTIVE |
| `/api/admin/redemptions/statistics` | GET | Get stats | ✅ ACTIVE |
| `/api/admin/redemptions/{id}/cancel` | PUT | Cancel one | ✅ ACTIVE |
| `/api/admin/redemptions/expire-bulk` | POST | Expire many | ✅ ACTIVE |
| `/api/admin/redemptions/by-code/{code}` | GET | Get by code | ❌ REMOVED |
| `/api/admin/redemptions/by-status/{status}` | GET | Filter by status | ❌ REMOVED |
| `/api/admin/redemptions/expired-codes` | GET | Get expired | ❌ REMOVED |
| `/api/admin/redemptions/{id}/expire` | PUT | Expire one | ❌ REMOVED |

---

