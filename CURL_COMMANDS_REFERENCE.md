# Quick Start - Curl Commands Reference

## Setup
Before running commands, make sure:
- Backend is running on `http://localhost:8080`
- You have a JWT token from login (set it in `TOKEN` variable)
- Replace `{{restaurantId}}`, `{{waitlistId}}` with actual IDs

```bash
# Set variables
export BASE_URL="http://localhost:8080/api"
export TOKEN="your_jwt_token_here"
export RESTAURANT_ID="1"
export WAITLIST_ID="5"
```

---

## 1. GET All Restaurants

**Endpoint**: `GET /api/restaurants`  
**Auth**: None  
**Purpose**: List all restaurants in database

```bash
curl -X GET "$BASE_URL/restaurants" \
  -H "Content-Type: application/json"
```

**Response** (Success):
```json
{
  "success": true,
  "message": "Restaurants retrieved",
  "data": [
    {
      "id": 1,
      "name": "thecommons",
      "address": "Canada",
      "phone": "+1-647-555-0101",
      "email": "thecommons@restaurant.com",
      "totalTables": 20,
      "createdAt": "2026-06-10T10:30:00",
      "updatedAt": "2026-06-10T10:30:00"
    }
  ]
}
```

---

## 2. GET SMS Templates (Admin)

**Endpoint**: `GET /api/admin/sms-templates`  
**Auth**: Admin JWT token required  
**Purpose**: View all SMS message templates

```bash
curl -X GET "$BASE_URL/admin/sms-templates" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

**Response**:
```json
{
  "success": true,
  "message": "SMS templates retrieved",
  "data": [
    {
      "id": 1,
      "templateType": "WAITLIST_NOTIFICATION",
      "messageTemplate": "Hi {guestName}, your table is almost ready! Estimated wait time: {estimatedWait} minutes.{position} Please come to the restaurant now.",
      "description": "Template for notifying guests when their table is almost ready",
      "createdAt": "2026-06-10T10:00:00",
      "updatedAt": "2026-06-10T10:00:00"
    },
    {
      "id": 2,
      "templateType": "SEATED_NOTIFICATION",
      "messageTemplate": "Hi {guestName}, your table is ready! Please proceed to the host stand. Thank you!",
      "description": "Template for confirming guest is seated",
      "createdAt": "2026-06-10T10:00:00",
      "updatedAt": "2026-06-10T10:00:00"
    }
  ]
}
```

---

## 3. UPDATE SMS Template (Admin)

**Endpoint**: `PUT /api/admin/sms-templates/{id}`  
**Auth**: Admin JWT token required  
**Purpose**: Customize SMS message templates

### Example 1: Update WAITLIST_NOTIFICATION (Template ID: 1)

```bash
curl -X PUT "$BASE_URL/admin/sms-templates/1" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "messageTemplate": "Hey {guestName}! 🎉 Your table is ready! Wait time: {estimatedWait} mins.{position}",
    "description": "Fun and casual notification message"
  }'
```

### Example 2: Update SEATED_NOTIFICATION (Template ID: 2)

```bash
curl -X PUT "$BASE_URL/admin/sms-templates/2" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "messageTemplate": "Welcome {guestName}! Your table is ready. Enjoy your meal!",
    "description": "Friendly welcomed message"
  }'
```

### Example 3: Formal Message

```bash
curl -X PUT "$BASE_URL/admin/sms-templates/1" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "messageTemplate": "Dear {guestName}, your table will be ready in approximately {estimatedWait} minutes. Your current position: {position}. Thank you for your patience.",
    "description": "Professional formal message"
  }'
```

**Response**:
```json
{
  "success": true,
  "message": "SMS template updated successfully",
  "data": {
    "id": 1,
    "templateType": "WAITLIST_NOTIFICATION",
    "messageTemplate": "Your updated message here",
    "description": "New description",
    "createdAt": "2026-06-10T10:00:00",
    "updatedAt": "2026-06-10T10:15:00"
  }
}
```

---

## 4. NOTIFY Guest (Enhanced) - Restaurant

**Endpoint**: `POST /api/restaurants/{restaurantId}/waitlist/{id}/notify`  
**Auth**: Restaurant JWT token required  
**Purpose**: Notify guest their table is almost ready

### Option A: Without Optional Parameters

When waitlist already has estimated time and position in database:

```bash
curl -X POST "$BASE_URL/restaurants/$RESTAURANT_ID/waitlist/$WAITLIST_ID/notify" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{}'
```

### Option B: With Estimated Wait Time and Position

Provide these values if they're not already in database:

```bash
curl -X POST "$BASE_URL/restaurants/$RESTAURANT_ID/waitlist/$WAITLIST_ID/notify" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "estimatedWaitTime": 15,
    "position": 2
  }'
```

### Option C: With Only Estimated Wait Time

```bash
curl -X POST "$BASE_URL/restaurants/$RESTAURANT_ID/waitlist/$WAITLIST_ID/notify" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "estimatedWaitTime": 20
  }'
```

### Option D: With Only Position

```bash
curl -X POST "$BASE_URL/restaurants/$RESTAURANT_ID/waitlist/$WAITLIST_ID/notify" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "position": 3
  }'
```

**Response**:
```json
{
  "success": true,
  "message": "Guest notified successfully"
}
```

---

## Complete Workflow Example

### Step 1: Get all restaurants
```bash
curl -X GET "$BASE_URL/restaurants"
```
Save the first restaurant ID

### Step 2: Admin updates SMS template
```bash
export ADMIN_TOKEN="admin_jwt_token"
curl -X PUT "$BASE_URL/admin/sms-templates/1" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "messageTemplate": "Hi {guestName}! Your table awaits. {estimatedWait} mins. Position: {position}.",
    "description": "New custom message"
  }'
```

### Step 3: Restaurant notifies guest
```bash
export RESTAURANT_TOKEN="restaurant_jwt_token"
curl -X POST "$BASE_URL/restaurants/1/waitlist/5/notify" \
  -H "Authorization: Bearer $RESTAURANT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "estimatedWaitTime": 10,
    "position": 1
  }'
```

**Guest receives SMS**:
```
Hi John! Your table awaits. 10 mins. Position: 1.
```

---

## Error Responses

### 400 Bad Request - Invalid Template
```json
{
  "success": false,
  "message": "SMS template not found: INVALID_TYPE"
}
```

### 403 Forbidden - Insufficient Permissions
```json
{
  "success": false,
  "message": "Access Denied"
}
```

### 404 Not Found - Restaurant or Waitlist
```json
{
  "success": false,
  "message": "Restaurant not found"
}
```

---

## Tips & Tricks

### 1. Pretty Print JSON Response
```bash
curl ... | jq .
```

### 2. Save Response to File
```bash
curl ... > response.json
```

### 3. Extract Data from Response
```bash
curl ... | jq '.data[0].id'
```

### 4. Check Response Headers
```bash
curl -i ...
```

### 5. Show Request Details
```bash
curl -v ...
```

### 6. Set Variables from Response
```bash
RESTAURANT_ID=$(curl ... | jq '.data[0].id')
echo "Restaurant ID: $RESTAURANT_ID"
```

---

## Debugging

### Check if Server is Running
```bash
curl http://localhost:8080/api/restaurants
```

### Check Token Validity
```bash
curl -X GET "$BASE_URL/admin/sms-templates" \
  -H "Authorization: Bearer $TOKEN" -v
```

### View Template IDs
```bash
curl "$BASE_URL/admin/sms-templates" \
  -H "Authorization: Bearer $TOKEN" | jq '.data[] | {id, templateType}'
```

### Test Message Formatting
Update template with test placeholders, then notify a guest to see if it formats correctly.

---

## Template Placeholders

Always use these exact placeholders in custom templates:

| Placeholder | What it becomes |
|------------|-----------------|
| {guestName} | Guest's name from DB |
| {estimatedWait} | Estimated wait time in minutes |
| {position} | " Your position: X." (if available) |

**Wrong**: `{guest_name}`, `{waiting_time}`, `{pos}`  
**Correct**: `{guestName}`, `{estimatedWait}`, `{position}`

---

## Batch Operations

### Update Multiple Templates
```bash
# Function to update template
update_template() {
  local ID=$1
  local MESSAGE=$2
  curl -X PUT "$BASE_URL/admin/sms-templates/$ID" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{\"messageTemplate\": \"$MESSAGE\"}"
}

# Update both templates
update_template 1 "Updated WAITLIST: Hi {guestName}, wait {estimatedWait}m{position}"
update_template 2 "Updated SEATED: Welcome {guestName}! Table ready!"
```

### Bulk Notify Guests
```bash
# Get all waitlist IDs and notify each
for WAITLIST_ID in 1 2 3 4 5; do
  curl -X POST "$BASE_URL/restaurants/1/waitlist/$WAITLIST_ID/notify" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"estimatedWaitTime": 15}'
  echo "Notified waitlist $WAITLIST_ID"
  sleep 1  # Delay to avoid rate limiting
done
```

---

## Common Scenarios

### Scenario 1: Guest Added by Admin (Has Position)
Admin used: `POST /api/restaurants/1/waitlist` with `position: 3`

Now notify with estimated time:
```bash
curl -X POST "$BASE_URL/restaurants/1/waitlist/5/notify" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"estimatedWaitTime": 15}'
```
Result: Position 3 from DB + estimatedTime 15 from request = Both in SMS

### Scenario 2: Guest Added by Public (No Position/Time)
Guest used: `POST /api/waitlist` (no position/time)

Now notify with both values:
```bash
curl -X POST "$BASE_URL/restaurants/1/waitlist/5/notify" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"estimatedWaitTime": 20, "position": 5}'
```
Result: Both values saved to DB and included in SMS

### Scenario 3: Everything Already in Database
Just notify without parameters:
```bash
curl -X POST "$BASE_URL/restaurants/1/waitlist/5/notify" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{}'
```
Result: Uses existing DB values in SMS


