# Server Rotation — How It Works & How To Test It

This document explains the "Server Rotation Logic" feature end-to-end: the fairness
algorithm, every API involved, the exact request/response shapes, and a step-by-step
functional test script you can run with `curl` (or import into Postman).

---

## 1. What problem this solves

> 2 servers, 4 tables.
> 1 customer → 1 server/table. 2 customers → 2 servers/tables.
> If a customer doesn't want to sit in a particular server's section, the **next**
> server gets the table — but the server who was skipped stays first in line for the
> **next** seating. The rotation pointer never silently skips anyone.

This is implemented as a classic "Next Up" round-robin:

- Every server who is currently **on the floor** sits in an ordered rotation list.
- Exactly **one** on-floor server is marked **Next Up**.
- When a party is seated:
  - If the host accepts the suggested (Next Up) server → that server's `seatedCount`
    goes up by 1, **and** the Next-Up pointer advances to the following server.
  - If the host instead manually picks a **different** on-floor server (because the
    guest asked for someone else, or for load-balancing) → that server's
    `seatedCount` goes up by 1, but the Next-Up pointer **does not move**. The
    skipped server is still first in line for the very next seating.
- A **manager** (or admin) can explicitly **"Start a new shift"**, which resets every
  on-floor server's `seatedCount` back to 0 and restarts the rotation from the first
  server in line. This is a **manual, on-demand action only** — there is no automatic
  midnight/daily reset.

### Example walkthrough (2 servers: Alice, Bob)
1. Both added to the floor. Alice is first → **Alice is Next Up**.
2. Party #1 seated with no preference → auto-assigned to Alice (Next Up). Alice
   `seatedCount = 1`. Pointer advances → **Bob is Next Up**.
3. Party #2 doesn't want Bob's section; host manually assigns Alice instead.
   Alice `seatedCount = 2`. Bob was Next Up and **stays** Next Up (out-of-turn pick
   does not advance the pointer).
4. Party #3 seated with no preference → auto-assigned to Bob (still Next Up).
   Bob `seatedCount = 1`. Pointer advances back to Alice.

---

## 2. Data model (new/changed)

| Entity | New fields | Purpose |
|---|---|---|
| `Staff` | role can now be `SERVER` | Identifies floor servers |
| `User` | role can now be `SERVER` | Login/JWT role for a server's own account |
| `Table` | `currentServerId` | Who is currently serving this table |
| `Waitlist` | `tableId`, `assignedServerId`, `orderNotes`, `orderNotesUpdatedAt` | Which table/server a seated party is linked to, plus free-text order notes |
| `ServerFloorStatus` (new) | `staffId`, `onFloor`, `seatedCount`, `rotationOrder`, `isNextUp` | The rotation state, one row per server |
| `ServerNotification` (new) | `staffId`, `waitlistId`, `tableId`, `type`, `message`, `isRead` | "You've been assigned Table X" feed for the server's own screen |

Migrations:
- `src/main/resources/db/migration/V8__server_rotation_and_table_assignment.sql`
- `src/main/resources/db/migration/V9__server_order_notes.sql`

---

## 3. All APIs involved

All endpoints require `Authorization: Bearer <JWT>` (from `/api/auth/login`) unless noted.
Base URL below assumes `http://localhost:8080`.

### 3.1 Setup/auth (existing APIs you'll reuse)

| # | Method & Path | Role | Purpose |
|---|---|---|---|
| 1 | `POST /api/auth/login` | public | Get a JWT for ADMIN/MANAGER/HOST/SERVER |
| 2 | `POST /api/admin/staff` | ADMIN | Invite a new staff member (use `"role": "SERVER"`) |
| 3 | `POST /api/admin/staff/set-password` | public (token) | Server accepts invite email & sets password → creates their `User` login |
| 4 | `POST /api/restaurants/{restaurantId}/tables` | ADMIN/MANAGER/HOST | Create floor-plan tables |
| 5 | `GET  /api/restaurants/{restaurantId}/tables` | ADMIN/MANAGER/HOST | List tables (now includes `currentServerId`) |
| 6 | `POST /api/restaurants/{restaurantId}/waitlist` | ADMIN/MANAGER/HOST | Add a guest to the waitlist (to seat later) |

### 3.2 Rotation / floor management (new — manager-facing)

| # | Method & Path | Role | Purpose |
|---|---|---|---|
| 7 | `GET  /api/restaurants/{restaurantId}/servers/floor` | ADMIN/MANAGER/HOST | View the rotation roster (who's on floor, seatedCount, who's Next Up) |
| 8 | `POST /api/restaurants/{restaurantId}/servers/{staffId}/floor` | ADMIN/MANAGER/HOST | Put a server **on** or **off** the floor. Body: `{ "onFloor": true }` |
| 9 | `POST /api/restaurants/{restaurantId}/servers/shift/start` | **ADMIN/MANAGER only** | Explicitly start a new shift — resets seatedCount for everyone on the floor |

### 3.3 Seating (existing API, now rotation-aware)

| # | Method & Path | Role | Purpose |
|---|---|---|---|
| 10 | `POST /api/restaurants/{restaurantId}/waitlist/{id}/seat` | ADMIN/MANAGER/HOST | Seat a guest. Body (all optional): `{ "tableName": "B2", "tableId": 5, "serverId": 12 }`. Leave `serverId` out to auto-assign to whoever is Next Up. |

### 3.4 Server's own screen (new — server-facing)

| # | Method & Path | Role | Purpose |
|---|---|---|---|
| 11 | `GET  /api/restaurants/{restaurantId}/servers/me/tables` | SERVER (or ADMIN/MANAGER/HOST) | "My Tables" — every currently-seated party assigned to **me** |
| 12 | `POST /api/restaurants/{restaurantId}/servers/me/tables/{waitlistId}/notes` | **SERVER only** | Save/update free-text **order notes** for one of my own currently-seated tables (what the guest wants, special requests, etc.) |
| 13 | `GET  /api/restaurants/{restaurantId}/servers/me/notifications?page=0&size=20` | SERVER | My notification feed ("You've been assigned Table 4...") |
| 14 | `GET  /api/restaurants/{restaurantId}/servers/me/notifications/unread-count` | SERVER | Badge count for the UI |
| 15 | `POST /api/restaurants/{restaurantId}/servers/me/notifications/{notificationId}/read` | SERVER | Mark a notification as read |

> Note: endpoints 11–15 resolve "me" from the JWT (via the linked `Staff` record), so
> the server does **not** pass their own staffId — they just log in as themselves.

All responses are wrapped in the standard envelope:
```json
{ "success": true, "message": "...", "data": { ... } }
```

---

## 4. Functional test script (curl)

Replace `TOKEN_ADMIN` / `TOKEN_SERVER_1` / `TOKEN_SERVER_2` with real JWTs after login,
and `RID` with your restaurant id.

### Step 0 — Login as an admin/manager/host
```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"manager@yourrestaurant.com","password":"yourpassword"}'
# => copy data.token into TOKEN_ADMIN
```

### Step 1 — Invite two SERVER staff members
```bash
curl -s -X POST http://localhost:8080/api/admin/staff \
  -H "Authorization: Bearer $TOKEN_ADMIN" -H "Content-Type: application/json" \
  -d '{"name":"Alice","role":"SERVER","locationId":RID,"email":"alice@test.com"}'

curl -s -X POST http://localhost:8080/api/admin/staff \
  -H "Authorization: Bearer $TOKEN_ADMIN" -H "Content-Type: application/json" \
  -d '{"name":"Bob","role":"SERVER","locationId":RID,"email":"bob@test.com"}'
# => note the returned staffId for each (data.id)
```
Each invited server gets an invitation email with a token. Use
`POST /api/admin/staff/set-password` with that token to create their login
(`{"token":"...","password":"Passw0rd!","confirmPassword":"Passw0rd!"}`), then they can
`POST /api/auth/login` with their own email/password to get `TOKEN_SERVER_1` / `_2`.

> **Shortcut for local testing** if you don't want to wire up email: insert the
> invitation token directly from the `staff_invitation_tokens` table, or temporarily
> call `setStaffPassword` with that token value.

### Step 2 — Create 4 tables
```bash
for n in A1 A2 B1 B2; do
curl -s -X POST http://localhost:8080/api/restaurants/RID/tables \
  -H "Authorization: Bearer $TOKEN_ADMIN" -H "Content-Type: application/json" \
  -d "{\"tableNumber\":\"$n\",\"capacity\":4}"
done
```

### Step 3 — Put both servers on the floor
```bash
curl -s -X POST http://localhost:8080/api/restaurants/RID/servers/<ALICE_STAFF_ID>/floor \
  -H "Authorization: Bearer $TOKEN_ADMIN" -H "Content-Type: application/json" \
  -d '{"onFloor": true}'

curl -s -X POST http://localhost:8080/api/restaurants/RID/servers/<BOB_STAFF_ID>/floor \
  -H "Authorization: Bearer $TOKEN_ADMIN" -H "Content-Type: application/json" \
  -d '{"onFloor": true}'
```

### Step 4 — Check the roster
```bash
curl -s http://localhost:8080/api/restaurants/RID/servers/floor \
  -H "Authorization: Bearer $TOKEN_ADMIN"
```
**Expect:** Alice first (added first) → `"onFloor": true, "seatedCount": 0, "nextUp": true`.
Bob → `"onFloor": true, "seatedCount": 0, "nextUp": false`.

### Step 5 — Add 3 guests to the waitlist
```bash
for i in 1 2 3; do
curl -s -X POST http://localhost:8080/api/restaurants/RID/waitlist \
  -H "Authorization: Bearer $TOKEN_ADMIN" -H "Content-Type: application/json" \
  -d "{\"name\":\"Guest $i\",\"phone\":\"555000000$i\",\"partySize\":2}"
done
# note each returned data.id as WID1, WID2, WID3
```

### Step 6 — Seat Guest 1 with NO server specified (auto Next Up)
```bash
curl -s -X POST http://localhost:8080/api/restaurants/RID/waitlist/WID1/seat \
  -H "Authorization: Bearer $TOKEN_ADMIN" -H "Content-Type: application/json" \
  -d '{"tableId": <A1_TABLE_ID>}'
```
**Expect:** `data.assignedServerId` == Alice's staffId.
Re-check the roster (Step 4's call) → Alice `seatedCount=1, nextUp=false`; Bob `nextUp=true`.

### Step 7 — Seat Guest 2 OUT OF TURN (guest didn't want Bob → explicitly request Alice)
```bash
curl -s -X POST http://localhost:8080/api/restaurants/RID/waitlist/WID2/seat \
  -H "Authorization: Bearer $TOKEN_ADMIN" -H "Content-Type: application/json" \
  -d '{"tableId": <A2_TABLE_ID>, "serverId": <ALICE_STAFF_ID>}'
```
**Expect:** `data.assignedServerId` == Alice's staffId, Alice `seatedCount=2`.
**Critical check:** re-check the roster → **Bob must still be `nextUp:true`** (the
pointer was NOT skipped/advanced because this was an out-of-turn pick).

### Step 8 — Seat Guest 3 with NO server specified again
```bash
curl -s -X POST http://localhost:8080/api/restaurants/RID/waitlist/WID3/seat \
  -H "Authorization: Bearer $TOKEN_ADMIN" -H "Content-Type: application/json" \
  -d '{"tableId": <B1_TABLE_ID>}'
```
**Expect:** `data.assignedServerId` == Bob's staffId (he was still Next Up from step 6,
proving he wasn't skipped). Bob `seatedCount=1`. Pointer now advances back to Alice.

### Step 9 — Merged-table group = ONE seating event
```bash
# Merge A1 & A2 (if still OPEN) then seat a party at the primary table:
curl -s -X POST http://localhost:8080/api/restaurants/RID/tables/merge \
  -H "Authorization: Bearer $TOKEN_ADMIN" -H "Content-Type: application/json" \
  -d '{"tableId": <TABLE_X>, "mergedTableId": <TABLE_Y>}'

curl -s -X POST http://localhost:8080/api/restaurants/RID/waitlist/WID4/seat \
  -H "Authorization: Bearer $TOKEN_ADMIN" -H "Content-Type: application/json" \
  -d '{"tableId": <TABLE_X>}'

curl -s http://localhost:8080/api/restaurants/RID/tables \
  -H "Authorization: Bearer $TOKEN_ADMIN"
```
**Expect:** Both `TABLE_X` and `TABLE_Y` show the **same** `currentServerId` and
`status: OCCUPIED` — confirming the merge group is credited as a single seating event
(only +1 to that one server's seatedCount, not +2).

### Step 10 — Server's own "My Tables" screen
```bash
curl -s http://localhost:8080/api/restaurants/RID/servers/me/tables \
  -H "Authorization: Bearer $TOKEN_SERVER_1"
```
**Expect (as Alice):** a list containing Guest 1 and Guest 2 (both her tables), with
`tableName`, `partySize`, `guestName`, `seatedAt`, `orderNotes` (null until step 10b).

### Step 10b — Server adds order notes to one of their tables
```bash
curl -s -X POST http://localhost:8080/api/restaurants/RID/servers/me/tables/WID1/notes \
  -H "Authorization: Bearer $TOKEN_SERVER_1" -H "Content-Type: application/json" \
  -d '{"orderNotes": "2x Caesar salad, 1x no onions, allergic to peanuts, extra napkins"}'
```
**Expect:** `200 OK`, response `data.orderNotes` echoes the text, `data.orderNotesUpdatedAt` is set.

Re-run Step 10 (`GET /servers/me/tables`) → Guest 1's entry now shows that `orderNotes` text.

**Negative checks:**
- Bob (`TOKEN_SERVER_2`) tries to add notes to Alice's `WID1` → **expect 400** ("You can
  only add order notes to tables assigned to you").
- Try adding notes to a waitlist id that isn't currently `SEATED` (e.g. already cleared) →
  **expect 400** ("Order notes can only be added while the party is seated").
- Try with a `waitlistId` belonging to a different restaurant → **expect 400**.

### Step 11 — Server's notifications
```bash
curl -s "http://localhost:8080/api/restaurants/RID/servers/me/notifications?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN_SERVER_1"

curl -s http://localhost:8080/api/restaurants/RID/servers/me/notifications/unread-count \
  -H "Authorization: Bearer $TOKEN_SERVER_1"
```
**Expect:** 2 notifications for Alice (one per seating), `unreadCount: 2`.

Mark one as read:
```bash
curl -s -X POST http://localhost:8080/api/restaurants/RID/servers/me/notifications/<NOTIFICATION_ID>/read \
  -H "Authorization: Bearer $TOKEN_SERVER_1"
```
**Expect:** `unread-count` drops to 1.

### Step 12 — Manager starts a new shift (manual reset only)
```bash
curl -s -X POST http://localhost:8080/api/restaurants/RID/servers/shift/start \
  -H "Authorization: Bearer $TOKEN_ADMIN"
```
**Expect:** Alice & Bob both `seatedCount: 0`; rotation restarts with the server who
was first in `rotationOrder` marked `nextUp: true` again.
**Also verify:** without calling this endpoint, seatedCount never resets on its own
(no automatic midnight job) — seat a few more guests on a "new day" and confirm counts
keep accumulating until you explicitly call this endpoint.

### Step 13 — Removing a server from the floor mid-shift
```bash
curl -s -X POST http://localhost:8080/api/restaurants/RID/servers/<BOB_STAFF_ID>/floor \
  -H "Authorization: Bearer $TOKEN_ADMIN" -H "Content-Type: application/json" \
  -d '{"onFloor": false}'

curl -s http://localhost:8080/api/restaurants/RID/servers/floor \
  -H "Authorization: Bearer $TOKEN_ADMIN"
```
**Expect:** Bob `onFloor:false`. If Bob was `nextUp` at the time, the pointer moves to
whoever else is on the floor (Alice). Seat a new guest with no `serverId` → it should
go to Alice automatically (Bob is correctly skipped because he clocked out, not because
of unfair rotation logic).

### Step 14 — No servers on the floor
Remove Alice too, then seat a guest with no `serverId` specified.
**Expect:** `data.assignedServerId` is `null` (seating still succeeds — the restaurant
can operate without the rotation feature active; no error is thrown).

### Step 15 — Permission checks
- Call `POST /servers/shift/start` with a **HOST** token → **expect 403** (host-only
  roles cannot start a shift, only ADMIN/MANAGER).
- Call `GET /servers/me/tables` with `TOKEN_SERVER_2` (Bob) → should only return
  **Bob's** tables, never Alice's.
- Call `POST /servers/me/tables/{waitlistId}/notes` with an **ADMIN/MANAGER/HOST** token
  → **expect 403** (this endpoint is SERVER-only).
- Call any `/servers/...` endpoint with no `Authorization` header → **expect 401/403**.

---

## 5. Edge cases already handled (verify during testing)

| Scenario | Expected behavior |
|---|---|
| `serverId` passed but that server is NOT on the floor | `400 Bad Request` — "Selected server is not currently on the floor" |
| `staffId` passed to `/floor` belongs to a different restaurant | `400 Bad Request` — "Staff member does not belong to the specified restaurant" |
| `staffId` passed to `/floor` is not role `SERVER` (e.g. a HOST) | `400 Bad Request` — "Only staff with role SERVER can be placed on the floor rotation" |
| Only 1 server on the floor | That server is always Next Up; seating always goes to them; pointer "advances" to itself |
| Table status manually set back to `OPEN`/`NEEDS_CLEANING` via `POST /tables/{id}/status` | `currentServerId` on that table is cleared automatically |
| Guest seated without `tableId` | Still works — only `tableName` (free text) is stored, no Table/server link, `assignedServerId` still set from rotation if servers are on the floor |
| Order notes on a table not assigned to the caller | `400 Bad Request` — "You can only add order notes to tables assigned to you" |
| Order notes on a party that isn't currently `SEATED` | `400 Bad Request` — "Order notes can only be added while the party is seated" |
| `orderNotes` longer than 2000 characters | `400 Bad Request` (validation) |

---

## 6. Quick reference — request/response shapes

**`POST /servers/{staffId}/floor`**
```json
{ "onFloor": true }
```

**`GET /servers/floor` → `data`**
```json
[
  { "staffId": 12, "name": "Alice", "onFloor": true, "seatedCount": 2, "rotationOrder": 1, "nextUp": false },
  { "staffId": 13, "name": "Bob",   "onFloor": true, "seatedCount": 1, "rotationOrder": 2, "nextUp": true }
]
```

**`POST /waitlist/{id}/seat`**
```json
{ "tableName": "B2", "tableId": 5, "serverId": 12 }
```
(all fields optional — omit `serverId` for auto Next-Up assignment)

**`GET /servers/me/tables` → `data`**
```json
[
  {
    "waitlistId": 101, "tableId": 5, "tableName": "B2",
    "guestName": "Guest 1", "partySize": 2,
    "notes": null, "preference": null,
    "seatedAt": "2026-10-10T19:05:00",
    "orderNotes": "2x Caesar salad, 1x no onions, allergic to peanuts",
    "orderNotesUpdatedAt": "2026-10-10T19:12:00"
  }
]
```

**`POST /servers/me/tables/{waitlistId}/notes`**
```json
{ "orderNotes": "2x Caesar salad, 1x no onions, allergic to peanuts, extra napkins" }
```
→ `data` is the same `ServerAssignedTableResponse` shape shown above, with the updated
`orderNotes` / `orderNotesUpdatedAt`.

**`GET /servers/me/notifications` → `data`** (Spring `Page` object)
```json
{
  "content": [
    {
      "id": 1, "waitlistId": 101, "tableId": 5,
      "type": "TABLE_ASSIGNED",
      "message": "You've been assigned B2 for a party of 2 (Guest 1).",
      "isRead": false, "createdAt": "2026-10-10T19:05:00"
    }
  ],
  "totalElements": 1, "totalPages": 1, "number": 0, "size": 20
}
```

