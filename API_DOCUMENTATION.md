# Waitlist Management System - API Documentation

**Base URL:** `http://localhost:8080/api`

**Authentication:** Include JWT token in `Authorization` header:
```
Authorization: Bearer <your_jwt_token>
```

---

## 🔓 Authentication Endpoints

### 1. Login
**Endpoint:** `POST /auth/login`

**Access:** Public

**Request:**
```json
{
  "email": "guest@example.com",
  "password": "123456"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "user": {
      "id": 1,
      "email": "guest@example.com",
      "name": "Dinesh",
      "phone": "9876543210",
      "role": "GUEST"
    }
  }
}
```

**Errors:**
- `400 Bad Request` - Invalid credentials or user not found

---

### 2. Forgot Password
**Endpoint:** `POST /auth/forgot-password`

**Access:** Public

**Request:**
```json
{
  "email": "guest@example.com"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Password reset email sent successfully",
  "data": null
}
```

---

### 3. Reset Password
**Endpoint:** `POST /auth/reset-password`

**Access:** Public

**Request:**
```json
{
  "token": "abc123xyz789",
  "newPassword": "newpassword123",
  "confirmPassword": "newpassword123"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Password reset successfully",
  "data": null
}
```

**Errors:**
- `400 Bad Request` - Token expired or invalid
- `400 Bad Request` - Passwords do not match

---

## 👤 Guest Waitlist Endpoints

### 4. Join Waitlist
**Endpoint:** `POST /waitlist`

**Access:** GUEST role required

**Request:**
```json
{
  "restaurantId": 1,
  "name": "Dinesh",
  "phone": "9876543210",
  "partySize": 3,
  "preference": "Indoor",
  "notes": "Birthday celebration"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Successfully joined waitlist",
  "data": {
    "id": 101,
    "guestName": "Dinesh",
    "guestPhone": "9876543210",
    "partySize": 3,
    "preference": "Indoor",
    "notes": "Birthday celebration",
    "status": "PENDING",
    "position": null,
    "estimatedWaitTime": null
  }
}
```

**Note:** Initial status is `PENDING`. Restaurant staff will approve and set position/wait time.

---

### 5. Check Waitlist Status
**Endpoint:** `GET /waitlist/status?phone=9876543210`

**Access:** Public

**Query Parameters:**
- `phone` (required): Guest phone number

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Waitlist status",
  "data": {
    "id": 101,
    "guestName": "Dinesh",
    "guestPhone": "9876543210",
    "partySize": 3,
    "preference": "Indoor",
    "notes": "Birthday celebration",
    "status": "WAITING",
    "position": 3,
    "estimatedWaitTime": 45
  }
}
```

**Statuses:**
- `PENDING` - Awaiting restaurant approval
- `WAITING` - Approved, waiting for table
- `NOTIFIED` - Restaurant notified, come now
- `SEATED` - Guest is seated
- `CANCELLED` - Guest cancelled
- `NO_SHOW` - Guest didn't show up

---

### 6. Leave Waitlist
**Endpoint:** `DELETE /waitlist/{id}`

**Access:** Public

**Path Parameters:**
- `id` (required): Waitlist entry ID

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Removed from waitlist",
  "data": null
}
```

---

### 7. Submit Feedback
**Endpoint:** `POST /feedback`

**Access:** GUEST role required

**Requirements:** Guest must have `SEATED` status

**Request:**
```json
{
  "waitlistId": 101,
  "rating": 5,
  "comments": "Excellent service and food!",
  "tags": ["Friendly staff", "Quick service", "Great food"]
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Feedback submitted successfully",
  "data": null
}
```

**Validation:**
- `rating` must be 1-5
- At least one tag recommended

---

## 🍽️ Restaurant Management Endpoints

### 8. Get Waitlist
**Endpoint:** `GET /restaurants/{restaurantId}/waitlist`

**Access:** RESTAURANT role required

**Path Parameters:**
- `restaurantId` (required): Restaurant ID

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Waitlist retrieved",
  "data": [
    {
      "id": 101,
      "guestName": "Dinesh",
      "guestPhone": "9876543210",
      "partySize": 3,
      "preference": "Indoor",
      "notes": "Birthday",
      "status": "WAITING",
      "position": 1,
      "estimatedWaitTime": 30
    },
    {
      "id": 102,
      "guestName": "Ravi",
      "guestPhone": "9999999999",
      "partySize": 2,
      "preference": "Patio",
      "notes": "VIP",
      "status": "WAITING",
      "position": 2,
      "estimatedWaitTime": 45
    }
  ]
}
```

---

### 9. Add Guest (Restaurant Side)
**Endpoint:** `POST /restaurants/{restaurantId}/waitlist`

**Access:** RESTAURANT role required

**Path Parameters:**
- `restaurantId` (required): Restaurant ID

**Request:**
```json
{
  "name": "Ravi",
  "phone": "9999999999",
  "partySize": 2,
  "preference": "Patio",
  "notes": "VIP Guest",
  "position": 6,
  "estimatedWaitTime": 30
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Guest added to waitlist",
  "data": {
    "id": 102,
    "guestName": "Ravi",
    "guestPhone": "9999999999",
    "partySize": 2,
    "preference": "Patio",
    "notes": "VIP Guest",
    "status": "WAITING",
    "position": 6,
    "estimatedWaitTime": 30
  }
}
```

**Note:** Position and estimated wait time are set manually by restaurant staff.

---

### 10. Notify Guest (Send SMS)
**Endpoint:** `POST /restaurants/waitlist/{id}/notify`

**Access:** RESTAURANT role required

**Path Parameters:**
- `id` (required): Waitlist entry ID

**Request:** None (empty body)

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Guest notified successfully",
  "data": null
}
```

**SMS Example:**
```
Hi Dinesh, your table is almost ready! Estimated wait time: 30 minutes. 
Please come to the restaurant now.
```

**Note:** Status automatically changes to `NOTIFIED`

---

### 11. Seat Guest
**Endpoint:** `POST /restaurants/waitlist/{id}/seat`

**Access:** RESTAURANT role required

**Path Parameters:**
- `id` (required): Waitlist entry ID

**Request:** None (empty body)

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Guest seated successfully",
  "data": null
}
```

**SMS Example:**
```
Hi Dinesh, your table is ready! Please proceed to the host stand. Thank you!
```

**Note:** Status changes to `SEATED`, `seatedAt` timestamp recorded

---

### 12. Remove Guest
**Endpoint:** `DELETE /restaurants/waitlist/{id}`

**Access:** RESTAURANT role required

**Path Parameters:**
- `id` (required): Waitlist entry ID

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Guest removed",
  "data": null
}
```

---

### 13. Get Tables
**Endpoint:** `GET /restaurants/{restaurantId}/tables`

**Access:** RESTAURANT role required

**Path Parameters:**
- `restaurantId` (required): Restaurant ID

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Tables retrieved",
  "data": [
    {
      "id": 1,
      "tableNumber": "T1",
      "capacity": 2,
      "status": "OPEN"
    },
    {
      "id": 2,
      "tableNumber": "T2",
      "capacity": 4,
      "status": "OCCUPIED"
    },
    {
      "id": 3,
      "tableNumber": "T3",
      "capacity": 6,
      "status": "RESERVED"
    }
  ]
}
```

**Table Statuses:**
- `OPEN` - Available
- `OCCUPIED` - Guest is seated
- `RESERVED` - Reserved for future

---

### 14. Update Table Status
**Endpoint:** `PUT /tables/{id}`

**Access:** RESTAURANT role required

**Path Parameters:**
- `id` (required): Table ID

**Request:**
```json
{
  "status": "OCCUPIED"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Table updated successfully",
  "data": null
}
```

---

## 📊 Admin Analytics Endpoints

### 15. Dashboard Analytics
**Endpoint:** `GET /admin/analytics`

**Access:** ADMIN role required

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Analytics data retrieved",
  "data": {
    "totalSeated": 34,
    "avgWaitTime": 22.5,
    "noShowRate": 5.9,
    "avgRating": 4.6
  }
}
```

---

### 16. Guest History
**Endpoint:** `GET /admin/guests`

**Access:** ADMIN role required

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Guest history retrieved",
  "data": [
    {
      "name": "Dinesh",
      "phone": "9876543210",
      "visits": 4,
      "avgWait": 18.5,
      "lastVisit": "2026-06-08",
      "rating": 4.5
    },
    {
      "name": "Ravi",
      "phone": "9999999999",
      "visits": 2,
      "avgWait": 25.0,
      "lastVisit": "2026-06-07",
      "rating": 4.0
    }
  ]
}
```

---

### 17. Feedback Insights
**Endpoint:** `GET /admin/feedback`

**Access:** ADMIN role required

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Feedback insights retrieved",
  "data": {
    "overallRating": 4.6,
    "totalReviews": 28,
    "topTags": [
      "Friendly staff",
      "Great food",
      "Quick service",
      "Clean restaurant",
      "Good ambiance"
    ]
  }
}
```

---

## 🔄 Request/Response Status Codes

| Code | Meaning |
|------|---------|
| **200** | Success |
| **201** | Created |
| **400** | Bad Request (validation error) |
| **401** | Unauthorized (no token) |
| **403** | Forbidden (insufficient role) |
| **404** | Not Found |
| **500** | Server Error |

---

## 🔐 Authorization Headers

**Include in all authenticated requests:**
```
Authorization: Bearer <jwt_token_from_login>
```

**Example:**
```bash
curl -X GET http://localhost:8080/api/waitlist/status?phone=9876543210 \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

---

## 🚨 Common Error Responses

**Validation Error (400):**
```json
{
  "success": false,
  "message": "Validation error: Phone: must not be blank",
  "data": null
}
```

**Not Found (404):**
```json
{
  "success": false,
  "message": "Waitlist entry not found",
  "data": null
}
```

**Unauthorized (401):**
```json
{
  "success": false,
  "message": "No token provided",
  "data": null
}
```

**Forbidden (403):**
```json
{
  "success": false,
  "message": "Access denied: Insufficient permissions",
  "data": null
}
```

---

## 📞 Contact & Support

For issues or questions about the API:
- Email: contact@waitlist-app.com
- Documentation: See SETUP_GUIDE.md

---

Last Updated: June 9, 2026

