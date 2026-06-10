# Complete API Reference - cURL Commands

## Base URL
```bash
BASE_URL="http://localhost:8080/api"
TOKEN="your-jwt-token"
RESTAURANT_ID="1"
WAITLIST_ID="1"
TABLE_ID="1"
PHONE="+1-416-555-0144"
```

## 🔐 Authentication

### Login
```bash
curl -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "restaurant@example.com",
    "password": "password123"
  }'
```

### Forgot Password
```bash
curl -X POST "$BASE_URL/auth/forgot-password" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "restaurant@example.com"
  }'
```

### Reset Password
```bash
curl -X POST "$BASE_URL/auth/reset-password" \
  -H "Content-Type: application/json" \
  -d '{
    "token": "reset-token-from-email",
    "newPassword": "newpassword123",
    "confirmPassword": "newpassword123"
  }'
```

## 🏥 Health Check (No Auth Required)
```bash
curl -X GET "$BASE_URL/health"
```

## 👤 Guest Operations

### Join Waitlist
```bash
curl -X POST "$BASE_URL/waitlist" \
  -H "Content-Type: application/json" \
  -d '{
    "restaurantId": 1,
    "name": "Jane Smith",
    "phone": "+1-416-555-0144",
    "partySize": 4,
    "preference": "Window Seat",
    "notes": "Vegetarian guests"
  }'
```

### Get Waitlist Status (All Records for Phone)
```bash
curl -X POST "$BASE_URL/waitlist/status" \
  -H "Content-Type: application/json" \
  -d '{
    "restaurantId": 1,
    "phone": "+1-416-555-0144"
  }'
```

### Leave Waitlist
```bash
curl -X DELETE "$BASE_URL/waitlist/$RESTAURANT_ID/$WAITLIST_ID"
```

### Submit Feedback
```bash
curl -X POST "$BASE_URL/feedback" \
  -H "Content-Type: application/json" \
  -d '{
    "waitlistId": 1,
    "rating": 5,
    "comments": "Excellent service!",
    "tags": ["Friendly staff", "Quick service"]
  }'
```

## 🍽️ Restaurant Setup & Management

### Get All Restaurants
```bash
curl -X GET "$BASE_URL/restaurants"
```

### Create Restaurant
```bash
curl -X POST "$BASE_URL/restaurants" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Restaurant Name",
    "address": "Address",
    "phone": "+1-647-555-0101",
    "email": "restaurant@example.com",
    "totalTables": 20
  }'
```

## 🏢 Restaurant Operations (Requires Auth)

### Get Restaurant Waitlist
```bash
curl -X GET "$BASE_URL/restaurants/$RESTAURANT_ID/waitlist" \
  -H "Authorization: Bearer $TOKEN"
```

### Add Guest to Waitlist (Staff)
```bash
curl -X POST "$BASE_URL/restaurants/$RESTAURANT_ID/waitlist" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "John Doe",
    "phone": "+1-416-555-0123",
    "partySize": 4,
    "preference": "Window Seat",
    "notes": "Allergic to nuts",
    "position": 2,
    "estimatedWaitTime": 20
  }'
```

### Notify Guest (Send SMS)
```bash
curl -X POST "$BASE_URL/restaurants/$RESTAURANT_ID/waitlist/$WAITLIST_ID/notify" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "estimatedWaitTime": 15,
    "position": 2
  }'
```

### Seat Guest
```bash
curl -X POST "$BASE_URL/restaurants/$RESTAURANT_ID/waitlist/$WAITLIST_ID/seat" \
  -H "Authorization: Bearer $TOKEN"
```

### Remove Guest from Waitlist
```bash
curl -X DELETE "$BASE_URL/restaurants/$RESTAURANT_ID/waitlist/$WAITLIST_ID" \
  -H "Authorization: Bearer $TOKEN"
```

## 📋 Table Management

### Get All Tables
```bash
curl -X GET "$BASE_URL/restaurants/$RESTAURANT_ID/tables" \
  -H "Authorization: Bearer $TOKEN"
```

### Add Table
```bash
curl -X POST "$BASE_URL/restaurants/$RESTAURANT_ID/tables" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "tableNumber": "T01",
    "capacity": 4
  }'
```

### Update Table Status
```bash
curl -X POST "$BASE_URL/restaurants/$RESTAURANT_ID/tables/$TABLE_ID/status?status=OCCUPIED" \
  -H "Authorization: Bearer $TOKEN"
```

**Status Options:** OPEN, OCCUPIED, RESERVED, NEEDS_CLEANING

## 📊 Dashboard & Live Updates

### Get Dashboard Stats (Waiting, Notified, Avg Wait, Seated Today, No-Shows, Table Status)
```bash
curl -X GET "$BASE_URL/restaurants/$RESTAURANT_ID/dashboard" \
  -H "Authorization: Bearer $TOKEN"
```

### Get Guest History
```bash
curl -X GET "$BASE_URL/restaurants/$RESTAURANT_ID/guest-history" \
  -H "Authorization: Bearer $TOKEN"
```

## ⚙️ Restaurant Settings

### Get Settings
```bash
curl -X GET "$BASE_URL/restaurants/$RESTAURANT_ID/settings" \
  -H "Authorization: Bearer $TOKEN"
```

### Update Settings
```bash
curl -X PUT "$BASE_URL/restaurants/$RESTAURANT_ID/settings" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "sendSmsNotifications": true,
    "sendEmailNotifications": true,
    "averageServiceTime": 45,
    "bufferTime": 15,
    "operatingHours": "10:00-22:00",
    "maxWaitlistSize": 50
  }'
```

**Fields:**
- `sendSmsNotifications` (Boolean) - Enable SMS notifications
- `sendEmailNotifications` (Boolean) - Enable email notifications
- `averageServiceTime` (Integer) - Average meal duration in minutes
- `bufferTime` (Integer) - Buffer time between guests in minutes
- `operatingHours` (String) - Format: "HH:MM-HH:MM"
- `maxWaitlistSize` (Integer) - Maximum guests in waitlist

## 📈 Admin Analytics (Admin Role Required)

### Get Dashboard Analytics
```bash
curl -X GET "$BASE_URL/admin/analytics" \
  -H "Authorization: Bearer $TOKEN"
```

### Get Guest History & Patterns
```bash
curl -X GET "$BASE_URL/admin/guests" \
  -H "Authorization: Bearer $TOKEN"
```

### Get Feedback Insights
```bash
curl -X GET "$BASE_URL/admin/feedback" \
  -H "Authorization: Bearer $TOKEN"
```

### Get SMS Templates
```bash
curl -X GET "$BASE_URL/admin/sms-templates" \
  -H "Authorization: Bearer $TOKEN"
```

### Update SMS Template
```bash
curl -X PUT "$BASE_URL/admin/sms-templates/1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "messageTemplate": "Hi {guestName}, your table is ready! Wait: {estimatedWait} min.{position}",
    "description": "Waitlist notification template"
  }'
```

**Template Placeholders:**
- `{guestName}` - Guest name
- `{estimatedWait}` - Estimated wait time in minutes
- `{position}` - Guest position in queue

## Usage Examples

### Full Workflow: Create → Add → Notify → Seat
```bash
# 1. Login
LOGIN=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"restaurant@example.com","password":"password123"}' | jq -r '.data.token')
TOKEN=$LOGIN

# 2. Create Restaurant
RESTAURANT=$(curl -s -X POST "$BASE_URL/restaurants" \
  -H "Content-Type: application/json" \
  -d '{"name":"My Restaurant","address":"Address","phone":"+1-647-555-0101","email":"rest@example.com","totalTables":20}' | jq -r '.data.id')
RESTAURANT_ID=$RESTAURANT

# 3. Add Guest
GUEST=$(curl -s -X POST "$BASE_URL/restaurants/$RESTAURANT_ID/waitlist" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"John","phone":"+1-416-555-0123","partySize":4,"estimatedWaitTime":20,"position":1}' | jq -r '.data.id')
WAITLIST_ID=$GUEST

# 4. Notify Guest
curl -s -X POST "$BASE_URL/restaurants/$RESTAURANT_ID/waitlist/$WAITLIST_ID/notify" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"estimatedWaitTime":15,"position":1}'

# 5. Seat Guest
curl -s -X POST "$BASE_URL/restaurants/$RESTAURANT_ID/waitlist/$WAITLIST_ID/seat" \
  -H "Authorization: Bearer $TOKEN"
```

