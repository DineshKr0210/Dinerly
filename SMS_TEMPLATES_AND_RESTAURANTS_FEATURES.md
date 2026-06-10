# Waitlist API - New Features Documentation

## Overview
This document describes the new features added to the Waitlist Management System backend.

---

## 1. Get All Restaurants API

### Endpoint
```
GET /api/restaurants
```

### Description
Retrieve a list of all restaurants from the database.

### Request
- **Method**: GET
- **Headers**: None required
- **Body**: Empty

### Response
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
    },
    {
      "id": 2,
      "name": "Social eatery",
      "address": "Canada",
      "phone": "+1-647-555-0102",
      "email": "socialeatery@restaurant.com",
      "totalTables": 25,
      "createdAt": "2026-06-10T10:31:00",
      "updatedAt": "2026-06-10T10:31:00"
    }
  ]
}
```

### Curl Example
```bash
curl -X GET http://localhost:8080/api/restaurants \
  -H "Content-Type: application/json"
```

---

## 2. Configurable SMS Message Templates

### Overview
SMS message templates are now stored in the database and can be customized by administrators without changing code.

### Supported Template Types
1. **WAITLIST_NOTIFICATION** - Sent when notifying guests their table is almost ready
2. **SEATED_NOTIFICATION** - Sent when confirming guest is seated

### Default Templates

#### WAITLIST_NOTIFICATION
```
Hi {guestName}, your table is almost ready! Estimated wait time: {estimatedWait} minutes.{position} Please come to the restaurant now.
```

**Placeholders:**
- `{guestName}` - Guest's name
- `{estimatedWait}` - Estimated wait time in minutes
- `{position}` - Current position in queue (optional, included if available)

#### SEATED_NOTIFICATION
```
Hi {guestName}, your table is ready! Please proceed to the host stand. Thank you!
```

**Placeholders:**
- `{guestName}` - Guest's name

---

## 3. Get SMS Templates API (Admin Only)

### Endpoint
```
GET /api/admin/sms-templates
```

### Description
Retrieve all SMS message templates for customization.

### Request
- **Method**: GET
- **Headers**: 
  - `Authorization: Bearer <token>` (Admin token required)
- **Body**: Empty

### Response
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

### Curl Example
```bash
curl -X GET http://localhost:8080/api/admin/sms-templates \
  -H "Authorization: Bearer <admin_token>"
```

---

## 4. Update SMS Template API (Admin Only)

### Endpoint
```
PUT /api/admin/sms-templates/{id}
```

### Description
Update an SMS message template. Admins can customize the message format without code changes.

### Request
- **Method**: PUT
- **Headers**: 
  - `Authorization: Bearer <token>` (Admin token required)
  - `Content-Type: application/json`
- **Path Parameters**:
  - `id` - Template ID (1 for WAITLIST_NOTIFICATION, 2 for SEATED_NOTIFICATION)
- **Body**:
```json
{
  "messageTemplate": "Custom message with {placeholders}",
  "description": "Description of the template (optional)"
}
```

### Response
```json
{
  "success": true,
  "message": "SMS template updated successfully",
  "data": {
    "id": 1,
    "templateType": "WAITLIST_NOTIFICATION",
    "messageTemplate": "Your new custom message template",
    "description": "Updated description",
    "createdAt": "2026-06-10T10:00:00",
    "updatedAt": "2026-06-10T10:15:00"
  }
}
```

### Examples

#### Example 1: Update WAITLIST_NOTIFICATION template
```bash
curl -X PUT http://localhost:8080/api/admin/sms-templates/1 \
  -H "Authorization: Bearer <admin_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "messageTemplate": "Hey {guestName}! Your table awaits. ETA: {estimatedWait} mins.{position}",
    "description": "Casual notification message"
  }'
```

#### Example 2: Update SEATED_NOTIFICATION template
```bash
curl -X PUT http://localhost:8080/api/admin/sms-templates/2 \
  -H "Authorization: Bearer <admin_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "messageTemplate": "Welcome {guestName}! Your table is ready. Enjoy your meal!",
    "description": "Friendly seated confirmation message"
  }'
```

---

## 5. Enhanced Notify Guest API

### Endpoint
```
POST /api/restaurants/{restaurantId}/waitlist/{id}/notify
```

### Description
Notify a guest their table is almost ready. Restaurant can optionally provide estimated wait time and position.

### Request
- **Method**: POST
- **Headers**: 
  - `Authorization: Bearer <token>` (Restaurant role required)
  - `Content-Type: application/json`
- **Path Parameters**:
  - `restaurantId` - Restaurant ID
  - `id` - Waitlist entry ID
- **Body** (optional):
```json
{
  "estimatedWaitTime": 15,
  "position": 2
}
```

### Behavior
1. If the waitlist entry **does NOT have** estimatedWaitTime in DB and the request provides it → Updates DB
2. If the waitlist entry **does NOT have** position in DB and the request provides it → Updates DB
3. If the waitlist entry **already has** these values in DB → Uses existing values (ignores request)
4. Marks the entry as NOTIFIED
5. Sends SMS using the configured template with final values

### Response
```json
{
  "success": true,
  "message": "Guest notified successfully"
}
```

### SMS Format Example
If template is: `"Hi {guestName}, your table is almost ready! Estimated wait time: {estimatedWait} minutes.{position} Please come to the restaurant now."`

And you call with:
```json
{
  "estimatedWaitTime": 15,
  "position": 2
}
```

The SMS sent will be:
```
Hi John Doe, your table is almost ready! Estimated wait time: 15 minutes. Your position: 2. Please come to the restaurant now.
```

### Curl Examples

#### Without optional parameters
```bash
curl -X POST http://localhost:8080/api/restaurants/1/waitlist/5/notify \
  -H "Authorization: Bearer <restaurant_token>" \
  -H "Content-Type: application/json" \
  -d '{}'
```

#### With estimated time and position
```bash
curl -X POST http://localhost:8080/api/restaurants/1/waitlist/5/notify \
  -H "Authorization: Bearer <restaurant_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "estimatedWaitTime": 15,
    "position": 2
  }'
```

---

## Database Schema

### SmsTemplate Table
```sql
CREATE TABLE sms_templates (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  template_type VARCHAR(50) UNIQUE NOT NULL,
  message_template TEXT NOT NULL,
  description TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

---

## Implementation Details

### Files Created/Modified

#### New Files
- `SmsTemplate.java` - Entity for SMS templates
- `SmsTemplateRepository.java` - Repository for DB operations
- `SmsTemplateService.java` - Business logic for templates
- `SmsTemplateInitializer.java` - Initializes default templates on app startup
- `UpdateSmsTemplateRequest.java` - DTO for update requests
- `SmsTemplateResponse.java` - DTO for responses
- `NotifyGuestRequest.java` - DTO for notify endpoint

#### Modified Files
- `SmsService.java` - Now fetches templates from DB and formats messages
- `AdminController.java` - Added SMS template endpoints
- `RestaurantController.java` - Updated notify endpoint to accept request body
- `RestaurantService.java` - Added getAllRestaurants() method, updated notifyGuest() logic
- `SecurityConfig.java` - Permits /api/waitlist/** for public guest operations

---

## Features Summary

| Feature | Endpoint | Method | Auth Required | Description |
|---------|----------|--------|---------------|-------------|
| Get All Restaurants | /api/restaurants | GET | No | List all restaurants |
| Get SMS Templates | /api/admin/sms-templates | GET | Admin | List all SMS templates |
| Update SMS Template | /api/admin/sms-templates/{id} | PUT | Admin | Customize SMS messages |
| Notify Guest (Enhanced) | /api/restaurants/{rid}/waitlist/{id}/notify | POST | Restaurant | Notify with optional time/position |

---

## Usage Flow

### As Admin (Customize SMS)
1. Login to get admin token
2. GET /api/admin/sms-templates to view current templates
3. PUT /api/admin/sms-templates/{id} to update template message
4. Changes apply automatically to all future SMS sent

### As Restaurant Staff (Notify Guest)
1. Login to get restaurant token
2. POST /api/restaurants/{restaurantId}/waitlist/{waitlistId}/notify with optional data
3. System updates DB if needed, marks as NOTIFIED, sends SMS using template
4. Guest receives personalized SMS with their wait time and position

---

## Placeholders Reference

### Available Placeholders in Templates

| Placeholder | Example | Notes |
|------------|---------|-------|
| {guestName} | John Doe | Guest's full name |
| {estimatedWait} | 15 | Wait time in minutes |
| {position} | Your position: 2. | Only included if position exists, has leading space |

### Custom Template Examples

#### Formal Style
```
Dear {guestName}, our team will have your table ready in approximately {estimatedWait} minutes. Your current position is {position}. Thank you for your patience.
```

#### Casual Style
```
Hey {guestName}! 🎉 You're up next! About {estimatedWait} mins to go.{position}
```

#### Concise Style
```
{guestName}: Table ready in {estimatedWait}m{position}
```

---

## Testing

### Test SMS Template Update
```bash
# Get current templates
curl http://localhost:8080/api/admin/sms-templates \
  -H "Authorization: Bearer <token>"

# Update template
curl -X PUT http://localhost:8080/api/admin/sms-templates/1 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "messageTemplate": "TEST: Hi {guestName}, wait {estimatedWait} mins{position}",
    "description": "Test template"
  }'

# Notify a guest and verify SMS uses new template
curl -X POST http://localhost:8080/api/restaurants/1/waitlist/1/notify \
  -H "Authorization: Bearer <restaurant_token>" \
  -H "Content-Type: application/json" \
  -d '{"estimatedWaitTime": 10, "position": 1}'
```

---

## Troubleshooting

### Templates Not Found
- Ensure the application has started (templates initialize on ApplicationReadyEvent)
- Check database for sms_templates table
- Default templates are created automatically on first startup

### SMS Not Sending
- Check Twilio credentials in application.properties
- Verify phone numbers are in correct format (E.164)
- Check server logs for SmsService errors

### Template Placeholders Not Replaced
- Ensure placeholder names match exactly (case-sensitive): {guestName}, {estimatedWait}, {position}
- Check for extra spaces inside braces
- Verify parameter is provided in notify request or exists in DB

---

## Future Enhancements

Potential improvements:
- Template versioning/history
- Multi-language support
- Per-restaurant custom templates
- Template preview/testing feature
- Analytics on SMS delivery success
- Template sharing between restaurants


