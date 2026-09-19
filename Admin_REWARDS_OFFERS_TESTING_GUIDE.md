

## 📋 Admin Rewards & Points Testing Flow - Complete API Guide

I've analyzed all admin controllers and created a comprehensive testing guide. Here's the summary:

### **1. POINTS MANAGEMENT** (10 Endpoints)
- ✅ Credit points to user
- ✅ Debit points from user
- ✅ Bulk credit multiple users
- ✅ Reverse previously awarded points
- ✅ Check user's point balance
- ✅ View user's points ledger/history
- ✅ Set exact balance directly
- ✅ Get points statistics
- ✅ Get top earners
- ✅ Bulk reverse points

### **2. POINTS EARNING RULES** (10 Endpoints)
- ✅ List all earning rules
- ✅ Create new rule (join_waitlist, dine_in, leave_review, refer_friend)
- ✅ Get specific rule
- ✅ Update rule points value
- ✅ Delete rule
- ✅ Get rules by action type
- ✅ Get available actions
- ✅ Toggle rule active/inactive
- ✅ Bulk update points values
- ✅ Get earning rules statistics

### **3. REWARD TIERS** (6 Endpoints)
- ✅ List all tiers (Silver, Gold, Platinum)
- ✅ Create new tier
- ✅ Get specific tier
- ✅ Update tier
- ✅ Delete tier
- ✅ Duplicate tier with new settings

### **4. WAYS TO EARN** (4 Endpoints)
- ✅ List ways to earn
- ✅ Create way to earn
- ✅ Update way to earn
- ✅ Delete way to earn

### **5. REWARD SETTINGS** (2 Endpoints)
- ✅ Get reward program settings
- ✅ Update settings (points per dollar, expiration, etc.)

### **6. REWARD ITEMS** (10 Endpoints)
- ✅ List reward items
- ✅ Create reward item (Free Coffee, $5 Off, etc.)
- ✅ Get specific item
- ✅ Update item
- ✅ Delete item
- ✅ Toggle availability
- ✅ Bulk toggle availability
- ✅ Get items by category
- ✅ Get available categories
- ✅ Duplicate item

### **7. RECEIPT CLAIMS MANAGEMENT** (15 Endpoints)
- ✅ List all receipt claims
- ✅ List pending claims only
- ✅ Get specific claim
- ✅ Get claim with full details
- ✅ Approve receipt claim
- ✅ Reject receipt claim
- ✅ Bulk approve claims
- ✅ Bulk reject claims
- ✅ Get claims by user
- ✅ Get claims by restaurant
- ✅ Find duplicate claims
- ✅ Export claims to CSV
- ✅ View receipt statistics
- ✅ Mark as duplicate
- ✅ Revert claim to pending

### **8. STATISTICS & ANALYTICS**
- ✅ Points statistics
- ✅ Rewards statistics
- ✅ Earning rules statistics
- ✅ User tier distribution
- ✅ Receipt claims statistics

---

## 🔑 Key Testing Workflows

### **Workflow 1: Setup Reward Program From Scratch**
1. Admin login → Get JWT token
2. Create 3 tiers (Silver, Gold, Platinum)
3. Create 4 earning rules (join_waitlist=10, dine_in=50, leave_review=25, refer_friend=100)
4. Create 3 reward items (Free Coffee=100pts, $5 Off=250pts, Free Dessert=500pts)
5. Launch campaign: Bulk credit 100 welcome points to all users
6. Check statistics

### **Workflow 2: Approve Receipt Claims (Admin Review)**
1. View pending claims: `GET /api/admin/receipt-claims/pending`
2. Review claim details: `GET /api/admin/receipt-claims/{claimId}/details`
3. Approve good claims: `PUT /api/admin/receipt-claims/{claimId}/approve`
4. Reject suspicious claims: `PUT /api/admin/receipt-claims/{claimId}/reject`
5. Bulk approve multiple: `POST /api/admin/receipt-claims/approve-bulk`
6. View statistics

### **Workflow 3: Run Promotion (5x Points)**
1. View current rules
2. Bulk update: Triple points for all earning actions
3. Run promotion for 7 days
4. Restore normal points
5. Check statistics

---

## 📊 Request/Response Examples

### **Example 1: Credit Points to User**
```bash
POST /api/admin/points/credit
Authorization: Bearer ADMIN_TOKEN
Content-Type: application/json

{
  "userId": 5,
  "amount": 100,
  "reason": "Welcome bonus"
}

Response:
{
  "success": true,
  "data": {
    "userId": 5,
    "pointsAdded": 100,
    "newBalance": 350
  }
}
```

### **Example 2: Create Points Earning Rule**
```bash
POST /api/admin/points/earning-rules
Authorization: Bearer ADMIN_TOKEN
Content-Type: application/json

{
  "action": "join_waitlist",
  "pointsValue": 10,
  "restaurantId": 1,
  "description": "Earn points for joining waitlist"
}

Response:
{
  "success": true,
  "data": {
    "id": 1,
    "action": "join_waitlist",
    "pointsValue": 10,
    "active": true
  }
}
```

### **Example 3: Create Reward Tier**
```bash
POST /api/admin/rewards/tiers
Authorization: Bearer ADMIN_TOKEN
Content-Type: application/json

{
  "name": "Gold",
  "pointsThreshold": 350,
  "tierOrder": 2,
  "restaurantId": 1,
  "perks": ["Priority seating", "10% bonus points"],
  "color": "gold"
}

Response:
{
  "success": true,
  "data": {
    "id": 2,
    "name": "Gold",
    "pointsThreshold": 350,
    "tierOrder": 2,
    "perks": [...]
  }
}
```

### **Example 4: Create Reward Item**
```bash
POST /api/admin/reward-items
Authorization: Bearer ADMIN_TOKEN
Content-Type: application/json

{
  "title": "Free Coffee",
  "description": "Any size, any blend",
  "pointsCost": 100,
  "restaurantId": 1,
  "category": "beverage",
  "available": true
}

Response:
{
  "success": true,
  "data": {
    "id": 1,
    "title": "Free Coffee",
    "pointsCost": 100,
    "category": "beverage",
    "available": true
  }
}
```

### **Example 5: Approve Receipt Claim**
```bash
PUT /api/admin/receipt-claims/101/approve
Authorization: Bearer ADMIN_TOKEN
Content-Type: application/json

{
  "pointsToAward": 85,
  "comments": "Receipt verified"
}

Response:
{
  "success": true,
  "data": {
    "id": 101,
    "userId": 5,
    "status": "APPROVED",
    "pointsAwarded": 85,
    "userNewBalance": 335
  }
}
```

### **Example 6: Bulk Approve Claims**
```bash
POST /api/admin/receipt-claims/approve-bulk
Authorization: Bearer ADMIN_TOKEN
Content-Type: application/json

{
  "claimIds": [101, 102, 103],
  "pointsOverride": null
}

Response:
{
  "success": true,
  "data": {
    "totalApproved": 3,
    "totalPointsAwarded": 290,
    "approvals": [...]
  }
}
```

---

## ⚙️ Role-Based Access Control

All admin endpoints require:
- ✅ Valid JWT token with role="ADMIN"
- ✅ `@PreAuthorize("hasRole('ADMIN')")` annotation
- ❌ Non-admin users get 403 Forbidden

---

## 📈 Key Operations Summary

| Operation | Endpoint | Method | Use Case |
|-----------|----------|--------|----------|
| Award points | `/api/admin/points/credit` | POST | Manual bonus, correction |
| Remove points | `/api/admin/points/debit` | POST | Fraud reversal, correction |
| Bulk award | `/api/admin/points/bulk-credit` | POST | Campaign, promotion |
| View balance | `/api/admin/points/balance/{userId}` | GET | Check user status |
| Create rule | `/api/admin/points/earning-rules` | POST | Setup earning mechanism |
| Update rule | `/api/admin/points/earning-rules/{id}` | PUT | Change points value |
| Create tier | `/api/admin/rewards/tiers` | POST | Setup tier levels |
| Create reward | `/api/admin/reward-items` | POST | Add redeemable items |
| Approve receipt | `/api/admin/receipt-claims/{id}/approve` | PUT | Award points for dine-in |
| View stats | `/api/admin/points/statistics` | GET | Monitor program |

---

]