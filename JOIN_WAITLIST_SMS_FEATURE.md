# Join Waitlist SMS Confirmation Feature

## Overview
When a guest joins the waitlist via the public `/api/waitlist` endpoint, they now automatically receive a confirmation SMS message thanking them for coming to the restaurant and letting them know they'll be notified when their table is ready.

## Implementation Details

### 1. New SMS Template: WAITLIST_JOIN_CONFIRMATION

**Template Type**: `WAITLIST_JOIN_CONFIRMATION`

**Message Template**:
```
Thank you {guestName}! We're delighted to have you at our restaurant. You have been added to our waitlist. We'll notify you as soon as your table is ready. We appreciate your patience!
```

**Placeholder**:
- `{guestName}` - Guest's name from the join request

### 2. Modified Files

#### WaitlistService.java
- **Added**: SmsService dependency injection
- **Updated**: `joinWaitlist()` method now sends confirmation SMS after saving waitlist entry
- **Code**:
  ```java
  @Autowired
  private SmsService smsService;
  
  public WaitlistResponse joinWaitlist(JoinWaitlistRequest request) {
      // ... existing code ...
      waitlist = waitlistRepository.save(waitlist);
      
      // Send join confirmation SMS
      smsService.sendJoinConfirmationSms(waitlist.getGuestPhone(), waitlist.getGuestName());
      
      return WaitlistResponse.fromWaitlist(waitlist);
  }
  ```

#### SmsService.java
- **Added**: `sendJoinConfirmationSms()` method
- **Code**:
  ```java
  public void sendJoinConfirmationSms(String phoneNumber, String guestName) {
      Map<String, String> params = new HashMap<>();
      params.put("guestName", guestName);

      String message = smsTemplateService.formatMessage("WAITLIST_JOIN_CONFIRMATION", params);
      sendSms(phoneNumber, message);
  }
  ```

#### SmsTemplateService.java
- **Added**: `initializeJoinConfirmationTemplate()` method
- **Code**:
  ```java
  public void initializeJoinConfirmationTemplate() {
      if (smsTemplateRepository.findByTemplateType("WAITLIST_JOIN_CONFIRMATION").isEmpty()) {
          SmsTemplate joinTemplate = SmsTemplate.builder()
                  .templateType("WAITLIST_JOIN_CONFIRMATION")
                  .messageTemplate("Thank you {guestName}! We're delighted to have you at our restaurant. You have been added to our waitlist. We'll notify you as soon as your table is ready. We appreciate your patience!")
                  .description("Template for confirming guest has joined the waitlist")
                  .build();
          smsTemplateRepository.save(joinTemplate);
      }
  }
  ```

#### SmsTemplateInitializer.java
- **Updated**: Now calls `initializeJoinConfirmationTemplate()` during application startup

## How It Works

### Flow Diagram
```
Guest calls POST /api/waitlist
         ↓
WaitlistService.joinWaitlist()
         ↓
1. Validate restaurant exists
2. Create Waitlist entry with PENDING status
3. Save to database
4. Send confirmation SMS
         ↓
Guest receives:
"Thank you Jane! We're delighted to have you at our restaurant. You have been added to our waitlist. We'll notify you as soon as your table is ready. We appreciate your patience!"
         ↓
Return success response
```

## Example

### Request
```bash
curl -X POST http://localhost:8080/api/waitlist \
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

### Response
```json
{
  "success": true,
  "message": "Successfully joined waitlist",
  "data": {
    "id": 1,
    "guestName": "Jane Smith",
    "guestPhone": "+1-416-555-0144",
    "partySize": 4,
    "preference": "Window Seat",
    "notes": "Vegetarian guests",
    "status": "PENDING",
    "createdAt": "2026-06-10T16:15:00"
  }
}
```

### SMS Sent to Guest
```
Thank you Jane Smith! We're delighted to have you at our restaurant. You have been added to our waitlist. We'll notify you as soon as your table is ready. We appreciate your patience!
```

## Database

### SMS Templates in Database
Three SMS templates are now automatically created:

1. **WAITLIST_NOTIFICATION** (For notifying guests table is ready)
   ```
   Hi {guestName}, your table is almost ready! Estimated wait time: {estimatedWait} minutes.{position} Please come to the restaurant now.
   ```

2. **SEATED_NOTIFICATION** (For confirming guest is seated)
   ```
   Hi {guestName}, your table is ready! Please proceed to the host stand. Thank you!
   ```

3. **WAITLIST_JOIN_CONFIRMATION** (For confirming guest joined)
   ```
   Thank you {guestName}! We're delighted to have you at our restaurant. You have been added to our waitlist. We'll notify you as soon as your table is ready. We appreciate your patience!
   ```

## Testing

### Manual Test
1. Start the backend application
2. Send POST request to `/api/waitlist` with guest details
3. Verify SMS is sent to the guest phone number
4. Check database `sms_templates` table for the new template (ID: 3)

### Test Data
```json
{
  "restaurantId": 1,
  "name": "Test Guest",
  "phone": "+1-416-555-9999",
  "partySize": 2,
  "preference": "Corner Table",
  "notes": "Celebrating anniversary"
}
```

## Admin Customization

Admins can now customize the join confirmation message via the API:

### GET Current Templates
```bash
curl -X GET http://localhost:8080/api/admin/sms-templates \
  -H "Authorization: Bearer <admin_token>"
```

### UPDATE Join Confirmation Template
```bash
curl -X PUT http://localhost:8080/api/admin/sms-templates/3 \
  -H "Authorization: Bearer <admin_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "messageTemplate": "Welcome {guestName}! Thank you for choosing us. Your spot is reserved in our waitlist. Sit back and relax - we will contact you shortly!",
    "description": "Custom join confirmation message"
  }'
```

## Build Status

✅ **Compilation**: Successful (0 errors)  
✅ **Build**: Successful  
✅ **JAR Size**: 69MB  

## Features Summary

| Feature | Status | Details |
|---------|--------|---------|
| Auto SMS on Join | ✅ | Sent immediately after joining |
| Configurable Template | ✅ | Admins can customize via API |
| Database Storage | ✅ | Template stored in sms_templates table |
| Auto Initialization | ✅ | Created on application startup |
| Placeholder Support | ✅ | Uses {guestName} placeholder |
| Error Handling | ✅ | Graceful failure if SMS service unavailable |

## Performance Impact

- **Minimal**: SMS sending is synchronous but uses external Twilio service
- **Future Enhancement**: Could implement async SMS sending using message queues

## Troubleshooting

### SMS Not Received
1. Verify Twilio credentials in `application.properties`
2. Check phone number format (should be E.164: +1-XXX-XXX-XXXX)
3. Verify Twilio account has credits
4. Check application logs for SMS sending errors

### Template Not Found
1. Verify application has fully started
2. Check `sms_templates` table has WAITLIST_JOIN_CONFIRMATION entry
3. Restart application if template is missing

### Wrong Message Format
1. Check the template in database
2. Use admin API to view/update template
3. Verify placeholders are spelled correctly ({guestName} is case-sensitive)

## Files Modified

1. ✅ WaitlistService.java
2. ✅ SmsService.java
3. ✅ SmsTemplateService.java
4. ✅ SmsTemplateInitializer.java

## Backward Compatibility

✅ **No breaking changes** - Existing APIs continue to work as before with the added SMS feature.

## Next Steps (Optional)

1. **Asynchronous SMS** - Use Spring async or message queues for better performance
2. **SMS Retry Logic** - Implement retry mechanism for failed SMS
3. **SMS Delivery Tracking** - Store SMS status in database
4. **Per-Restaurant Templates** - Allow each restaurant custom template
5. **Multi-Language Support** - Support templates in different languages


