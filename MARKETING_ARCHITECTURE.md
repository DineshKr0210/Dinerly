# Marketing & Campaign Architecture — Dinerly Backend

## Overview
This document describes the end-to-end marketing/campaign flow, how SMS/email sending works, and step-by-step instructions to test the endpoints locally or against a staging SMS provider (e.g., Twilio).

## Components
- API (this backend): campaign/template CRUD, publish workflow, metrics endpoints.
- Database: `campaigns` table stores campaigns, reach, redemptions, revenue, status, scheduledAt, restaurantId.
- Sender worker/service: background job that picks up SCHEDULED/A CTIVE campaigns and pushes messages to SMS/Email provider.
- SMS Provider: e.g., Twilio — receives requests to send messages and invokes delivery/webhook callbacks.
- Webhooks: delivery receipts / inbound replies update campaign metrics and redemptions.

## Key Entities
- Campaign: id, name, channel, audience, templateId, message, restaurantId, scheduledAt, status, sentCount, reach, redemptions, revenueInfluenced, createdAt.
- Template: message body with placeholders (currently stored in templates table/entity).

## Authorization
- All admin endpoints are under `/api/admin/*` and require `ROLE_ADMIN` (send `Authorization: Bearer <token>` with a JWT containing `role: "ADMIN"`).
- Public read endpoints for menus require `ROLE_GUEST` or `ROLE_ADMIN`.

## Endpoints (important ones)
- Create template: `POST /api/admin/campaigns/templates` (body: template payload)
- Create campaign: `POST /api/admin/campaigns` (body: CampaignRequest, include `locationId`)
- Update campaign: `PUT /api/admin/campaigns/{id}`
- Publish campaign (enqueue/send): `POST /api/admin/campaigns/{id}/publish`
- List campaigns (admin): `GET /api/admin/campaigns?page=&size=&locationId=&status=&channel=`
- Marketing summary: `GET /api/admin/marketing/summary?locationId=`
- Rewards & Offers performance: `GET /api/admin/performance/rewards-offers?locationId=&period=&page=&size=`

## End-to-end flow (Create → Send → Metrics)
1. Admin creates or selects a template (`/api/admin/campaigns/templates`).
2. Admin creates campaign (`POST /api/admin/campaigns`) with `locationId` and optional `scheduledAt`.
3. Campaign status initially `DRAFT` or `SCHEDULED`.
4. Admin publishes campaign (`POST /api/admin/campaigns/{id}/publish`). Backend sets status to `SCHEDULED` or `ACTIVE` and enqueues the campaign.
5. Sender worker picks up SCHEDULED/ACTIVE campaigns at scheduled time and calls SMS provider API (e.g., Twilio `messages.create`).
6. Provider responds with message SID; backend stores `sentCount`, `reach`, and maps message SID for future delivery updates.
7. Provider posts delivery webhook callbacks to backend (e.g., `/api/webhooks/twilio/delivery`) — backend updates `redemptions`/delivery-state and aggregates `revenueInfluenced` when redemptions are confirmed.
8. Admin views KPIs via `GET /api/admin/marketing/summary` and `GET /api/admin/performance/rewards-offers`.

## How the backend sends SMS (typical implementation)
1. Sender worker (scheduled task or separate microservice) queries campaigns with status `SCHEDULED` and `scheduledAt <= now`.
2. For each campaign, resolve audience to phone numbers (query guests/contacts by segment or restaurant guests list).
3. For each phone number, call provider SDK or REST endpoint (Twilio) with message body and from number.
4. Record provider message id and increment `sentCount`.
5. Optionally persist a send queue entry (send_jobs table) for retries and audit.

## What to configure to test SMS sending
- Provider credentials (example env vars): `TWILIO_ACCOUNT_SID`, `TWILIO_AUTH_TOKEN`, `TWILIO_FROM_NUMBER`.
- Callback URL (in provider dashboard): `https://<your-host>/api/webhooks/twilio/delivery` (for local testing use ngrok or localtunnel).

## Local testing checklist (step-by-step)
1. Start the backend:
```
./mvnw -DskipTests spring-boot:run
```
2. Ensure DB is seeded with a restaurant/locations and some guest phone numbers (or create test guests via API).
3. Obtain an admin JWT (create or generate via auth endpoints). Example header: `Authorization: Bearer <ADMIN_JWT>`.
4. Create a template (curl example):
```
curl -X POST "http://localhost:8080/api/admin/campaigns/templates" \
  -H "Authorization: Bearer $ADMIN_JWT" \
  -H "Content-Type: application/json" \
  -d '{"name":"Offer A","body":"Use CODE10 for 10% off"}'
```
5. Create a campaign (attach `locationId`):
```
curl -X POST "http://localhost:8080/api/admin/campaigns" \
  -H "Authorization: Bearer $ADMIN_JWT" \
  -H "Content-Type: application/json" \
  -d '{"name":"Sept Promo","channel":"SMS","audience":"all_guests","templateId":1,"locationId":123,"scheduledAt":null}'
```
6. Publish campaign:
```
curl -X POST "http://localhost:8080/api/admin/campaigns/<<campaignId>>/publish" \
  -H "Authorization: Bearer $ADMIN_JWT"
```
7. If your sender is a scheduled background task, either wait for the job or trigger the sender manually (depends on implementation). If using a manual endpoint to send, call that.
8. Verify sends: check `sentCount` and `reach` fields in the campaign record (`GET /api/admin/campaigns/{id}`) and provider message logs.
9. Simulate delivery callbacks: use provider console or send an HTTP POST to your webhook endpoint with the provider's payload shape. Confirm backend updates `redemptions` or delivery status.
10. Check metrics endpoints:
```
curl -H "Authorization: Bearer $ADMIN_JWT" "http://localhost:8080/api/admin/marketing/summary?locationId=123"

curl -H "Authorization: Bearer $ADMIN_JWT" "http://localhost:8080/api/admin/performance/rewards-offers?locationId=123&period=last7days&page=0&size=20"
```

## Example payloads
- CampaignRequest (minimal):
```
{
  "name": "Sept Promo",
  "channel": "SMS",
  "audience": "all_guests",
  "templateId": 1,
  "locationId": 123,
  "scheduledAt": "2026-09-10T10:00:00"
}
```

## Verifying SMS delivery & redemptions
- In Twilio: Use Messaging > Logs to see message SIDs and statuses.
- Configure webhook to receive delivery status updates; backend should map SID → campaign and update `redemptions` when redemption events occur (or when you receive reply keywords).
- For local development, use `ngrok http 8080` and set the webhook URL in Twilio to `https://<ngrok-id>.ngrok.io/api/webhooks/twilio/delivery`.

## Testing notes & tips
- Use a sandbox/test Twilio account to avoid production sends.
- Seed test guests with your personal phone number to verify message content.
- If the backend uses an internal sender queue table, inspect `send_jobs` to confirm jobs were created.
- For performance tests, create multiple campaigns/large audiences and use the metrics endpoints to confirm aggregation correctness.

## Troubleshooting
- 403 Forbidden: check JWT `role` claim maps to `ROLE_ADMIN` — JwtFilter maps `role` claim to `ROLE_<role>`.
- No messages sent: verify sender worker is running and provider credentials are set.
- Webhook not received: check ngrok/port forwarding and provider callback configuration.

## Next actions I can do for you
- Run the backend and execute the curl steps against your environment.
- Wire up a Twilio test account and demonstrate a full send flow with ngrok.
- Add automated integration tests that mock provider responses.

--
File: MARKETING_ARCHITECTURE.md
