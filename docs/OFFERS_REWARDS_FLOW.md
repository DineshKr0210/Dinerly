# Offers & Rewards — Functional Flow and Architecture

This document describes end-to-end functional flows for Offers & Rewards (Admin + Guest), redemption, points management, and the architecture. It lists the HTTP endpoints to hit step-by-step and includes a system architecture diagram.

**Audience:** Developers, QA, and DevOps.

**Assumptions**
- Backend base: Spring Boot app exposing REST endpoints under `/api/*`.
- Auth: JWT with `role` claim mapped to `ROLE_<role>`; principal is user email.
- DB: Relational (Postgres). Flyway runs migrations on app start.
- Notifications: SMS via Twilio through `NotificationService`/`SmsService`.
- Points: `DinerlyPoints` (per-user) and `PointsLedger` (audit).

**Key Entities (DB)**
- `offer` (Offer): id, name, restaurant_id, start_date, end_date, status (DRAFT/SCHEDULED/PUBLISHED/EXPIRED), points_cost, inventory, value, created_by
- `dinerly_points`: user_id, balance, version, created_at, updated_at
- `points_ledger`: id, user_id, delta, balance_after, reason, reference_id, created_by, created_at
- `redemption`: id, user_id, offer_id, status (REQUESTED/COMPLETED/REVERSED), points, created_at

**High-level flows**
1. Admin creates an Offer (DRAFT) and optionally schedules it.
2. Admin publishes campaign (immediate or scheduled). Scheduled campaigns are polled by `CampaignScheduler` which transitions `SCHEDULED` -> `PUBLISHED` and triggers sends.
3. When campaign sends, `NotificationService` builds message (template fallback), calls `SmsService` to send SMS, and records send status.
4. Guests list/view Offers. If user authenticated, backend computes `redeemable` by comparing `DinerlyPoints.balance` >= `Offer.pointsCost`.
5. Guest redeems: backend validates eligibility (balance, inventory, expiry), debits points (transactional + optimistic locking), creates `Redemption`, and sends notification. Ledger entry created.
6. Admin can credit, bulk-credit, or reverse points. Bulk-credit may be queued for large lists.

**Step-by-step endpoints and example payloads**

**Admin — Offers & Campaigns**
- Create Offer (DRAFT)
  - POST /api/admin/offers
  - Body: { "name":"Lunch Promo", "restaurantId": 1, "startDate":"2026-09-10T00:00:00Z", "endDate":"2026-09-30T23:59:59Z", "pointsCost": 100, "inventory": 100, "value":"20% off" }
  - Response: 201 with Offer payload

- Update Offer
  - PUT /api/admin/offers/{id}

- Publish Offer / Campaign
  - POST /api/admin/campaigns/{offerId}/publish
  - Body: { "immediate": true|false, "scheduledAt": "2026-09-12T14:00:00Z" }
  - If `immediate=true` publish now. If false and `scheduledAt` set, status -> SCHEDULED.

- List Offers (admin)
  - GET /api/admin/offers?restaurantId=1&status=DRAFT

**Admin — Points management**
- Credit points (single)
  - POST /api/admin/points/credit
  - Body: { "userId": 123, "amount": 500, "reason":"Welcome bonus" }
  - Result: `PointsService.credit(userId, amount, reason, adminId)` creates ledger entry and updates `dinerly_points`.

- Bulk credit (admin)
  - POST /api/admin/points/bulk-credit
  - Body: { "userIds": [123,124], "amount": 100, "reason":"Promo" }
  - Implementation: small lists handled synchronously; large lists should enqueue job on queue (Rabbit/Kafka) and return job id.

- Reverse points
  - POST /api/admin/points/reverse
  - Body: { "userId": 123, "amount": 100, "reason":"Correction", "referenceId": "txn-456" }
  - Result: credit back and log ledger with `referenceId`.

**Guest — Offers & Redemption**
- List Offers
  - GET /api/offers?locationId=1&status=PUBLISHED
  - If authenticated, `redeemable` boolean computed per-offer in `GuestOfferResponse`.

- Get Offer
  - GET /api/offers/{id}

- Redeem Offer (guest)
  - POST /api/offers/{offerId}/redeem
  - Authenticated user required. Body: { "quantity":1 }
  - Server-side checks (step-by-step):
    1. Load Offer by id; fail 404 if not found.
    2. Validate Offer.status == PUBLISHED and now within start/end dates.
    3. Check inventory: if `inventory != null` then ensure inventory >= quantity.
    4. Load `DinerlyPoints` for user (create row with 0 if not exists).
    5. Check `balance >= pointsCost * quantity`.
    6. Perform transactional debit with optimistic locking (versioned `dinerly_points`) and write `points_ledger` entry.
    7. Decrement offer inventory (transactional) and create `redemption` record with status `COMPLETED` (or `REQUESTED` if async fulfillment required).
    8. Trigger notification via `NotificationService` (SMS/email) — include redemption code.
    9. Return 200 with redemption summary.

  - Possible failure responses:
    - 400 if insufficient points or inventory
    - 403 if guest not allowed
    - 409 if concurrent inventory/debit conflict (retry recommended)

**Campaign sending flow (publish -> send)**
- When publish executed (immediate or scheduler):
  - `AdminCampaignService.publishCampaign(offerId, immediate)` does:
    - Transition offer.status -> PUBLISHED
    - If sending needs to target users: build audience (e.g., guests opt-in, location filter)
    - For each user in audience: create send job record or call `NotificationService.sendSms(...)` (prefer a queue for scale)
- `CampaignScheduler` periodically finds `SCHEDULED` campaigns where scheduledAt <= now and calls publish.
- `NotificationService` builds message: uses `Offer.message` or `SmsTemplate` fallback, resolves variables (guest.name, offer.code).
- `SmsService` uses Twilio; on send result, update `redemption` / `waitlist` rows or `send_history` and `pointsLedger` if redeem-as-send.

**DB & Concurrency considerations**
- `dinerly_points` must have `@Version` column for optimistic locking. On optimistic failure, retry a small number (e.g., 3) backoff.
- Use DB transaction to update `dinerly_points`, `points_ledger`, and `redemption` atomically where possible.
- Inventory decrement must be atomic: use `@Version` or `UPDATE ... WHERE inventory >= ?` pattern and check affected rows.
- Bulk operations (bulk-credit / mass sends) should use background jobs with idempotency keys.

**Notifications & Auditing**
- Every points change must create a `points_ledger` entry with `reason` and `referenceId` linking to admin action or redemption.
- All sends (SMS) should be stored (send_history) with status and external provider id.
- Expose admin endpoints to query `points_ledger` filtered by user and date range: `GET /api/admin/points/{userId}/ledger?start=&end=`.

**Security & Roles**
- Admin endpoints under `/api/admin/**` require `ROLE_ADMIN`.
- Guest endpoints: `/api/offers` and `/api/offers/{id}` public (but show `redeemable` only when authenticated). Redeem requires `ROLE_GUEST` (or at least authenticated user).
- Rate-limit redemption endpoint to prevent abuse.

**Observability & Metrics**
- Track metrics: `offers.published`, `offers.redeemed`, `points.credited`, `campaigns.sent`, `sms.success`, `sms.failure`.
- Add logs for publish/send/ledger operations with correlation id (request id).

**Errors & Edge Cases**
- If `DinerlyPoints` row missing, initialize with zero before checks.
- On optimistic lock failure in debit, retry and reconcile ledger to avoid double-debits.
- If notification send fails after debit, mark `redemption` as `ERROR` and provide admin reversal flow.

**Architecture diagram (mermaid)**

```mermaid
flowchart LR
  subgraph Client
    A[Admin UI]
    B[Guest App]
  end

  subgraph API
    API[Spring Boot REST API]
    AdminC[AdminControllers]
    GuestC[GuestControllers]
    PointsS[PointsService]
    OfferRepo[OfferRepository]
    CampaignSched[CampaignScheduler]
    NotificationS[NotificationService]
    SmsS[SmsService (Twilio)]
  end

  subgraph Infra
    DB[(Postgres DB)]
    Queue[(Message Queue)]
    Flyway[Flyway migrations]
    Metrics[Prometheus/Grafana]
  end

  A -->|admin calls| AdminC
  B -->|guest calls| GuestC
  AdminC --> API
  GuestC --> API
  AdminC --> OfferRepo
  GuestC --> OfferRepo
  AdminC --> PointsS
  GuestC --> PointsS
  PointsS --> DB
  OfferRepo --> DB
  CampaignSched --> AdminC
  AdminC -->|enqueue| Queue
  CampaignSched -->|enqueue| Queue
  Queue --> NotificationS
  NotificationS --> SmsS
  SmsS -->|provider| Twilio[TWILIO]
  NotificationS --> DB
  AdminC --> DB
  GuestC --> DB
  Flyway --> DB
  API --> Metrics
```

**Suggested next implementation tasks**
- Implement background job for large bulk credits and campaign sends (Queue + Worker).
- Add idempotency keys to admin credit and campaign publish endpoints.
- Add unit/integration tests for `PointsService` concurrency.
- Harden redeem flow: add per-guest daily limits, offer per-user limits, and inventory reservation TTL.

---

File created: `docs/OFFERS_REWARDS_FLOW.md`

If you want, I can also:
- Add OpenAPI examples for each endpoint in the codebase.
- Generate Postman collection for these endpoints.
- Implement the background queue worker for bulk-credit and campaign sends.

Which of those next?