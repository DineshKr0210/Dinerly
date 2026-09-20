# Dinerly Admin Portal — Full Requirements

**Document Purpose:** Complete specification of every tab and subtab in the Dinerly Admin Portal prototype, prepared for development handoff.

**Overview:** This document walks the entire admin portal prototype screen by screen, in the same order as the left sidebar, followed by the two features with sub-navigation (Performance, Settings). Each entry shows what's on the screen today and any build notes for engineering. Section 18 lists cross-cutting open items.

---

## Table of Contents

1. [Home (Dashboard)](#1-home-dashboard)
2. [Locations](#2-locations)
3. [Menu Management](#3-menu-management)
4. [Admin](#4-admin)
5. [Offers](#5-offers)
6. [Rewards Program](#6-rewards-program)
7. [Redemptions](#7-redemptions)
8. [Reviews](#8-reviews)
9. [Performance](#9-performance)
10. [Reports](#10-reports)
11. [Customers](#11-customers)
12. [Marketing](#12-marketing)
13. [Store Availability](#13-store-availability)
14. [Statements](#14-statements)
15. [Settings](#15-settings)
16. [Help](#16-help)
17. [Dashboard Insight Content Reference](#17-dashboard-insight-content-reference)
18. [Open Items for Engineering](#18-open-items-for-engineering)

---

## 1. Home (Dashboard)

**Navigation Path:** Sidebar → Home

**Overview:** Landing screen showing today's KPIs and insights feed.

### Key Sections

#### KPI Cards (Above Fold)
- **Waitlist joins today** - Count metric with comparison to yesterday
- **Active waitlists** - Current number
- **Redemptions today** - Count of redemptions
- **Average rating** - Star rating with count

#### Insights and Actions Feed
- **Filterable by:** All / Growth / Operations / Tips / Announcements
- **Content Requirements:** Covered in Section 17 (Dashboard Insight Content Reference)
- Each insight card can contain location-specific action buttons

#### Quick Actions Panel
- Create offer
- Edit menu item
- Edit store hours
- Add staff member
- Generate report
- Help center

#### Your Locations Table (Below Fold)
| Column | Content |
|--------|---------|
| LOCATION | Location name |
| STATUS | Open/Closed indicator |
| COVERS TODAY | Number of seated guests |
| AVG WAIT | Average wait time |
| RATING | Star rating |
| ACTION | "Manage" link |

**Important:** The "Manage" link opens the Location details modal (Section 2), rather than routing to the shared Performance page.

#### Operations Glance Panel
Shows live account-wide metrics:
- No-show rate
- Average notify-to-seated time
- Menu items missing photos

**Note:** Confirm whether these metrics should be live account-wide; currently static in prototype.

#### Growth Insight Card
References locations without active offers, showing join-rate lift data.

**Screenshot Reference:** Dashboard shows main portal interface with Brothers Café branding, all KPI cards, insights feed with filterable tabs, quick actions section, locations table, and operations panels.

---

## 2. Locations

**Navigation Path:** Sidebar → Management → Locations

**Overview:** Card-based location management with detail modal.

### Locations List View

One card per location displaying:
- Location nickname (large heading)
- Address
- Tags/categories
- Status toggle (Open/Closed/Inactive)
- "View details" button
- Status indicator with toggle switch
- "Add location" button in sidebar

**Important:** Add location opens a blank version of the same form used in the details modal.

### Location Details Modal

Opens from:
- Card's "View details" button
- Dashboard's Manage → link
- Insights card's location-specific action button

**Modal Form Fields:**

| Field | Type | Details |
|-------|------|---------|
| Location nickname | Text input | Unique identifier for location |
| Address | Text input | Full street address |
| Menu template | Dropdown | Reuse another location's menu or start blank |
| Seats | Numeric input | Seating capacity |
| Phone number | Text input | Location contact line |
| Manager email | Text input | Location-level manager contact |
| Owner name | Text input | Franchise owner of record |
| Owner email | Text input | Franchise owner email (may differ from account owner) |
| Location open | Toggle | Controls whether guests can join waitlist / see wait times |

**Modal Actions:**
- **Save changes** — Persists edits, closes modal
- **View performance** — Closes modal, routes to Performance pre-filtered to this location

**Screenshot Reference:** Shows modal overlay with all form fields, location-specific settings, and action buttons. Multiple location cards visible in background (Brothers Café locations with different initials: BC, CA, SB, ED).

---

## 3. Menu Management

**Navigation Path:** Sidebar → Management → Menu management

**Overview:** Per-location menu editor with category grouping.

### Key Features

- **Location switcher** at top scopes entire page to one location's menu
- **Category groups** (Breakfast, Mains, etc.) with expandable sections
- **Add category** link per group

### Menu Item Row Structure

Each item displays:
- Category name (section header)
- Item name (bold)
- Item description (smaller text)
- Price (right-aligned)
- Availability toggle (on/off switch)
- Edit action (pencil icon)
- Delete action (trash icon)
- **Add item** link per category
- **Add item** button (floating, top-right)

### Items & Photography

- Items without a photo show a camera placeholder icon
- **Open Item:** Confirm whether photo upload is in scope for v1

**Example Categories:**
- Breakfast
  - Classic Breakfast Combo - Eggs, toast, hash browns - $12.99
  - Bottomless coffee - Freshly ground, filter-included - $4.99
- Mains
  - Weekend Skillet - Charred peppers, potatoes, fried egg - $14.50
  - Sunday Brunch Plate - Waffles, seasonal fruit, whipped cream - $14.99

**Screenshot Reference:** Menu management interface with Brothers Café Osborne Village selected, showing Breakfast and Mains categories with items, prices, and availability toggles.

---

## 4. Admin

**Navigation Path:** Sidebar → Management → Admin

**Overview:** Staff directory with role and location management.

### Staff Directory Table

| Column | Content |
|--------|---------|
| NAME | Staff member name with avatar |
| ROLE | Owner / Manager / Staff |
| LOCATION | Location assignment |
| EMAIL | Contact email |
| STATUS | Active / Invited / Inactive |

### Key Features

- **Add staff member** button (top-right)
- Invites a new user to the system
- Displays all staff across all locations

### Important Notes

**Critical:** The "Owner" role pill should map to the account-level Owner in Settings → Profile, NOT a per-location owner. Keep this distinct from the per-location "Owner name/email" field added in Section 2 (Locations).

### Example Staff

| Name | Role | Location | Email | Status |
|------|------|----------|-------|--------|
| Hathran A. | Owner | All locations | hathran@dinely.co | Active |
| Jordan M. | Manager | Osborne Village | jordan.m@dinely.co | Active |
| Riley T. | Manager | Corylan Ave | riley.t@dinely.co | Active |
| Sam K. | Host | Exchange District | sam.k@dinely.co | Invited |

**Screenshot Reference:** Shows staff directory table with Brothers Café team members, roles, location assignments, and status indicators.

---

## 5. Offers

**Navigation Path:** Sidebar → Management → Offers

**Overview:** Coupon and deal management system.

### Key Features

- Active and scheduled offers per location
- Redemption count tracking
- Edit offers with status toggle
- View offers across multiple locations
- Filter by location

### Offer Card Display

Each offer card shows:
- Location identifier (BC, CA, SB, ED, etc.)
- Status badge (Active / Scheduled / Inactive)
- Offer title
- Description
- Start/end dates
- Edit action (pencil icon)
- Toggle switch (active/inactive)

### Locations Shown

- **BC** - Brothers Café, Osborne Village
- **CA** - Brothers Café, Corylan Ave
- **SB** - Brothers Café, St. Boniface
- **ED** - Brothers Café, Exchange District

### Insight Integration

Feeds the Growth insight card on the Dashboard (flags locations without an active offer).

**Screenshot Reference:** Offers management showing multiple location cards, each with active coupon/deal information including "Classic Breakfast Combo," "Pasta night," and "First waitlist jam" with edit options and date ranges.

---

## 6. Rewards Program

**Navigation Path:** Sidebar → Management → Rewards program

**Overview:** Loyalty tier configuration system.

### Tier Structure

Three default tiers with customizable settings:

| Tier | Color | Point Threshold | Perks |
|------|-------|-----------------|-------|
| Silver | Silver | 0 points | Beer for Earn points on every visit |
| Gold | Gold | 250-350 points | 1-2+ points on every visit, birthday reward |
| Platinum | Platinum | 500+ points | 1.5+ points, priority visited, Wine discount monthly |

### Sections

#### Tiers
- Tier cards showing name, points required, and key perks
- Edit functionality per tier
- Visual color coding per tier
- "+ Add rule" link for tier customization

#### Ways to Earn
- "Dined without joining a waitlist? (receipt scan)" - +15 pts, with edit/delete
- "Join the waitlist" - +10 pts, with edit/delete
- "Leave a review" - +20 pts, with edit/delete
- "Refer a friend" - +50 pts, with edit/delete
- "Dine 5 times in a month" - +100 pts, with edit/delete
- "+ Add rule" link to create new earning rules

#### Redemption Catalog
- Reward items with point costs
- Edit and delete actions
- Examples:
  - Free coffee - 100 pts
  - Free appetizer - 150 pts
  - Free dessert platter - 250 pts
- "+ Add reward" button

### Features

- Edit button per item
- Delete button per item
- Edit mode allows updating titles, descriptions, point costs
- "+ Add rule" and "+ Add reward" create new entries

**Dashboard Integration:** Referenced by the Announcements insight card (e.g., new tier announcements).

**Screenshot Reference:** Rewards program dashboard showing tier cards (Silver at 0 points, Gold at 250-350 points, Platinum at 500+ points) with descriptions, ways to earn section listing various actions and point values, and redemption catalog with item costs.

---

## 7. Redemptions

**Navigation Path:** Sidebar → Management → Redemptions

**Overview:** Log of all reward and coupon redemptions.

### Redemptions Table

| Column | Content |
|--------|---------|
| CODE | Redemption code (e.g., 298918, 912829) |
| TYPE | Reward / Coupon |
| ITEM | What was redeemed (e.g., "Bottomless coffee", "Pasta night") |
| LOCATION | Which location redeemed |
| GUEST | Guest name who redeemed |
| REDEEMED | Timestamp (e.g., "2 min ago", "34 min ago") |
| VALUE | Point cost or discount value |

### Filters

- "All locations" dropdown filter
- "Past week" date range filter
- Tabs: All / Coupons / Rewards

### Important Notes

**Critical:** Guest names here should stay distinct from staff/owner names used elsewhere in the portal to avoid confusion in data.

### Example Data

| Code | Type | Item | Location | Guest | Redeemed | Value |
|------|------|------|----------|-------|----------|-------|
| 298918 | Reward | Bottomless coffee | Osborne Village | Megan E. | 2 min ago | $2.50 off |
| 912829 | Coupon | Pasta night | Corylan Ave | Riley T. | 34 min ago | 20% off |
| 573849 | Reward | $5 off your bill | Osborne Village | Sam K. | 3 hrs ago | 150 pts |
| 681820 | Coupon | Kids meal or adult entree | Exchange District | Ava P. | Yesterday | Kids free |
| 384351 | Reward | Free dessert | Corylan Ave | Deryn L. | Yesterday | 200 pts |

**Screenshot Reference:** Redemptions log showing transaction history with codes, types, items, locations, guest names, and timestamps.

---

## 8. Reviews

**Navigation Path:** Sidebar → Management → Reviews

**Overview:** Guest reviews across all locations with reply functionality.

### Reviews Dashboard

#### Summary Cards (Top)
- **Average rating** - Star rating (e.g., 4.7 ★)
- **Total reviews** - Count (e.g., 2,041)
- **Needs reply** - Count of unreplied reviews (e.g., 3)
- **Reply rate** - Percentage (e.g., 91%)

### Review List Display

Each review shows:
- Guest avatar and name (e.g., "Sara K. - Osborne Village")
- Review date (e.g., "Aug 26, 2026")
- Star rating (1-5 stars)
- Review text/comment
- "Replied by: BROTHERS CAFÉ" with reply text
- "Print reply" button
- Edit/delete actions

### Filters

- "All locations" dropdown
- "All" / "Needs reply" / "5 ★" / "3 ★ & under" tabs

### Example Reviews

**Review 1:**
- Guest: Sara K., Osborne Village, 5 stars
- Comment: "Food was solid but we wanted almost 20 minutes just for our notified time to actually get seated. Would come back but with the notify-to-seated window was more accurate."
- Reply: "Thank you for your feedback..."
- Date: Aug 26, 2026

**Review 2:**
- Guest: Deryn L., Corylan Ave, 2 stars
- Comment: "Never got seated — so showed the wait after 40 min with no update to the app. Might have been an issue but the lack of communication was frustrating."
- Date: Jul 28, 2026

**Screenshot Reference:** Reviews management showing summary KPIs, individual review cards with guest names, ratings, comments, staff replies, and reply actions.

---

## 9. Performance

**Navigation Path:** Sidebar → Performance

**Overview:** Shared analytics dashboard with three sub-tabs for detailed metrics.

### Key Features

- **Location filter** - Defaults to "All locations"; pre-filters when opened via "View performance" from location details modal
- **Date-range filter** - Customizable time period selection

### 9a. Waitlist Sub-tab

#### KPI Cards (Top)
- **Waitlist joins (7 days)** - e.g., 912, with % change from prior week
- **Seated (7 days)** - e.g., 803, with % change from prior week
- **Avg wait time** - e.g., 14 min, with comparison to prior week

#### Charts

**Waitlist over time** - Line chart showing:
- Join count over selected date range (This week vs Prior week comparison lines)
- Time axis: Mon, Tue, Wed, Thu, Fri, Sat, Sun
- Toggle options: Joins / Seated / Avg wait

#### Location Leaderboard Table

| Rank | Location | Waitlist Joins | Redemptions | Avg Wait | Rating |
|------|----------|----------------|------------|----------|--------|
| 1 | Osborne Village | 1,120 | 34 | -12 min | 4.8 ★ |
| 2 | Corylan Ave | 880 | 22 | No wait | 4.6 ★ |

**Open Item:** Charts/KPIs currently show demo data regardless of location selected — location filter needs to actually scope underlying query.

### 9b. Reviews Sub-tab

#### KPI Cards (Top)
- **New reviews (7 days)** - e.g., 24, with % change
- **Average rating** - e.g., 4.7 ★, with % change
- **Reply rate** - e.g., 91%, with % change

#### Charts

**Reviews over time** - Line chart showing:
- Review volume and avg rating trend over date range
- Toggle: New reviews / Avg rating
- Comparison: This week vs Prior week

#### Location Leaderboard Table
Same structure as Waitlist tab

### 9c. Rewards & Offers Sub-tab

#### KPI Cards (Top)
- **Redemptions (7 days)** - e.g., 78, with % change
- **Points issued (7 days)** - e.g., 3,480, with % change
- **Active offers** - e.g., 3, with % change

#### Charts

**Rewards & offers over time** - Line chart showing:
- Redemptions and points issued trend
- Toggle: Redemptions / Points issued
- Comparison: This week vs Prior week

#### Location Leaderboard Table
Same structure as other tabs

**Screenshot Reference:** Performance dashboard showing three sub-tabs (Waitlist, Reviews, Rewards & Offers), location filter set to all locations, KPI cards with change indicators, line charts with trend data, and location leaderboard rankings.

---

## 10. Reports

**Navigation Path:** Sidebar → Reports

**Overview:** Generated report list with download/export functionality.

### Report Generation Section

#### Scope Selection
- **Overall report** - All locations combined, one file
- **Location report** - Single location, broken down by day

#### Period Selection
- Dropdown: Past month (default option)

#### Includes
- Waitlist joins & wait times
- Coupon and reward redemptions
- Offer performance analysis
- Customer ratings & reviews volume

#### Actions
- **"Generate & download CSV"** button

### Recent Reports Table

| Report | Scope | Period | Generated | Action |
|--------|-------|--------|-----------|--------|
| Overall_Report_Jun2028.csv | All locations | Past month | Jul 1, 2026 | Download |
| OsbourneVillage_Report_Q2.csv | Osborne Village | Last 3 months | Jun 15, 2026 | Download |

**Screenshot Reference:** Reports page showing report generation interface with scope and period selectors, and a list of previously generated reports with download links.

---

## 11. Customers

**Navigation Path:** Sidebar → Customers

**Overview:** Guest directory with visit history and loyalty tier information.

### Summary Cards (Top)

| Card | Metric |
|------|--------|
| Total unique guests | 1,616, with change from last month |
| New customers (250) | 250, % of active guests |
| Regular customers | 1,004, % of active guests |
| Avg visits per regular | 4.6, with trend |

### Recent Guests Table

| Column | Content |
|--------|---------|
| GUEST | Guest name with avatar |
| LOCATION(S) | Where they visit |
| VISITS | Total visit count |
| FIRST VISIT | Date of first visit |
| LAST VISIT | Date of most recent visit |
| STATUS | Regular / New |

### Example Data

| Guest | Locations | Visits | First Visit | Last Visit | Status |
|-------|-----------|--------|-------------|-----------|--------|
| Jordan M. | Osborne Village | 6 | Feb 3, 2026 | Jul 10, 2026 | Regular |
| Riley T. | Corylan Ave, Osborne Village | 4 | Apr 22, 2026 | Jul 6, 2026 | Regular |
| Sam K. | Osborne Village | 1 | Jul 17, 2026 | Jul 17, 2026 | New |
| Ava P. | Exchange District | 3 | May 21, 2026 | Jun 21, 2026 | Regular |
| Deryn L. | Corylan Ave | 1 | Jun 14, 2026 | Jun 14, 2026 | New |

**Screenshot Reference:** Customers page showing guest summary metrics, and recent guests table with visit history and status indicators.

---

## 12. Marketing

**Navigation Path:** Sidebar → Marketing

**Overview:** Campaign tools for targeted email/SMS messaging.

### Summary Cards (Top)

| Card | Metric |
|------|--------|
| Active campaigns | 3, with change |
| Guests reached (250) | 4,920, with % reach |
| Redemptions from campaigns | 186, with % of last month |
| Spend this month | CA$420, with % trend |

### Campaigns Table

| Column | Content |
|--------|---------|
| CAMPAIGN | Campaign name with icon |
| CHANNEL | Email / SMS / Email + push |
| REACH | Number of recipients |
| REDEMPTIONS | Number of redemptions |
| DATES | Campaign date range |
| STATUS | Active / Scheduled / Ended |

### Example Campaigns

| Campaign | Channel | Reach | Redemptions | Dates | Status |
|----------|---------|-------|-------------|-------|--------|
| Weekend happy hour push | Email + push | 2,180 | 64 | Jul 5 - Jul 31, 2026 | Active |
| Win back repeat guests | Email | 1,380 | 41 | Jun 20 - Jul 20, 2026 | Active |
| Leave a review, earn 50 pts | In-app | 1,400 | 81 | Jul 1 - Aug 1, 2026 | Active |
| Grand opening — Exchange District | Email + push | — | — | Aug 1 - Aug 14, 2026 | Scheduled |
| Mother's Day brunch offer | Email | 3,280 | 212 | May 1 - May 12, 2026 | Ended |

### Campaign Creation

- **"+ Create campaign"** button (top-right)
- Allows segmentation by tier or visit recency

**Screenshot Reference:** Marketing dashboard showing campaign summary cards, active campaigns table with channels (Email, Email + push, In-app), reach and redemption metrics, date ranges, and status indicators.

---

## 13. Store Availability

**Navigation Path:** Sidebar → Store availability

**Overview:** Hours and open/closed status management per location.

### Key Features

- **Location selector** at top to scope page to specific location
- **Currently open** toggle - Marks location as closed regardless of scheduled hours
- **Regular hours** section - Standard operating hours by day
- **Upcoming special hours** section - Scheduled closures and extended hours

### Regular Hours Display

| Day | Hours | Status |
|-----|-------|--------|
| Monday | 7:00 AM - 3:00 PM | Enabled |
| Tuesday | 7:00 AM - 3:00 PM | Enabled |
| Wednesday | 7:00 AM - 3:00 PM | Enabled |
| Thursday | 7:00 AM - 3:00 PM | Enabled |
| Friday | 7:00 AM - 8:00 PM | Enabled |
| Saturday | 8:00 AM - 8:00 PM | Enabled |
| Sunday | Closed | Disabled |

### Special Hours

- "+ Add special hours" button
- List of upcoming scheduled closures and extended hours
- Edit/delete actions per entry

### Dashboard Integration

Feeds the "closed with no reopen time" Operations insight card on the Dashboard (flags locations that are closed with no scheduled reopen).

**Screenshot Reference:** Store availability page showing location selector, currently open toggle, regular operating hours by day with time ranges and enable/disable toggles, and section for upcoming special hours.

---

## 14. Statements

**Navigation Path:** Sidebar → Statements

**Overview:** Billing history and invoice management for Dinerly subscription.

### Current Plan Section

- Plan name and tier (e.g., "Multi-location")
- Monthly cost (e.g., $249/month)
- Billing period date range
- Plan details and location count included
- "Manage plan" link

### Payment Method

- Payment icon (e.g., "VISA")
- Last 4 digits and expiry
- Change link

### Invoice History Table

| Column | Content |
|--------|---------|
| INVOICE | Invoice ID (e.g., INV-2026-0717) |
| DATE | Issue date |
| AMOUNT | Invoice amount (e.g., $249.00) |
| STATUS | Paid / Pending / Overdue |
| ACTION | Download link |

### Example Invoices

| Invoice | Date | Amount | Status | Action |
|---------|------|--------|--------|--------|
| INV-2026-0717 | Jul 17, 2026 | $249.00 | Paid | Download |
| INV-2026-0617 | Jun 17, 2026 | $249.00 | Paid | Download |
| INV-2026-0517 | May 17, 2026 | $200.00 | Paid | Download |
| INV-2026-0417 | Apr 17, 2026 | $200.00 | Paid | Download |

**Screenshot Reference:** Statements page showing current plan details with monthly cost, payment method card on file with last 4 digits, and invoice history table with dates, amounts, and download options.

---

## 15. Settings

**Navigation Path:** Sidebar → Settings

**Overview:** Account-level controls with 7 sub-tabs in left settings navigation.

### Settings Navigation (Left Sidebar)

1. Profile
2. Notifications
3. Advanced
4. Preferences
5. Security
6. Privacy & data
7. Help

---

### 15a. Profile

**Purpose:** Account owner info and franchise-level identity.

#### Account Section

| Field | Content | Action |
|-------|---------|--------|
| Owner name | Text display | Change link |
| Billing email | Email display | Change link |
| Plan | Subscription tier and location count | Manage plan link |

**Screenshot Reference:** Profile settings showing account owner name, billing email, and subscription plan with manage link.

---

### 15b. Notifications

**Purpose:** Notification channels and alert preferences.

#### Notification Channels

| Channel | Description | Status |
|---------|-------------|--------|
| Email | Email notifications enabled | Toggle |
| SMS text message | Text message notifications | Toggle |
| Push notifications | App/browser push alerts | Toggle |

#### Waitlist & Activity Alerts

| Alert Type | Description | Status |
|------------|-------------|--------|
| Daily performance summary | Daily digest sent at 6:00 AM | Toggle |
| Weekly report ready | Weekly report notification | Toggle |
| Location closed with no reopen time | Alert when location is closed without scheduled reopen | Toggle |
| Menu item unavailable 24 days | Alert when items missing photos/hiding them | Toggle |

#### Default Table-Ready Voice Message

**Multi-line text field** - Pre-filled with default script.

**Important Features:**
- Supports merge fields: `{guest_name}` and `{restaurant_name}`
- Fields are substituted at call time
- This is the script read by automated call when guest's table is ready

**Example Default:**
```
Hi, this is Dinely calling for {guest_name}. Your table at {restaurant_name} is ready. 
Please head to the host stand within the next 10 minutes.
```

**Screenshot Reference:** Notifications settings showing toggles for Email, SMS, and Push channels, waitlist & activity alert options, and multi-line text field for default table-ready voice message.

---

### 15c. Advanced

**Purpose:** Advanced/technical account settings.

#### Integrations Section

| Integration | Description | Status |
|------------|-------------|--------|
| POS integration | Not connected | Connect button |
| API access | Generate a key for custom integrations | Manage keys button |

#### Danger Zone Section

- **"Remove a location from your account"** button (prominent red)
- Warning text about permanent deletion and performance data
- "Remove a location" action button

**Screenshot Reference:** Advanced settings showing integrations (POS, API access) and danger zone with location removal warning and action button.

---

### 15d. Preferences

**Purpose:** Display and locale preferences.

#### Display Settings

| Setting | Options | Current |
|---------|---------|---------|
| Dark mode | Toggle | Applies to admin panel only |
| Default party size | Dropdown/number | Pre-filled in waitlist add (e.g., 4-6 people) |

#### Locale Settings

| Setting | Options | Current |
|---------|---------|---------|
| Time zone | Dropdown | Change link to modify |

**Screenshot Reference:** Preferences settings showing dark mode toggle, default party size field, and time zone selector with change link.

---

### 15e. Security

**Purpose:** Password, two-factor authentication, and session controls.

#### Security Controls

| Control | Description | Action |
|---------|-------------|--------|
| Password | Last changed X months ago | Change link |
| Two-factor authentication | Requires a code at sign-in, in addition to password | Toggle |
| Manager PIN | Optional PIN for staff facing pages | Regenerate link |

**Screenshot Reference:** Security settings showing password last changed date with change link, two-factor authentication toggle, and manager PIN with regenerate button.

---

### 15f. Privacy & Data

**Purpose:** Data export/deletion and privacy controls.

#### Data Management

| Option | Description | Action |
|--------|-------------|--------|
| Export all data | Download a CSV archive of all locations, guests, reports | Request export link |
| Data retention | Guest and performance history kept for 24 months | Change link |
| Privacy policy | Review Dinerly's current privacy policy and terms | View link |

**Screenshot Reference:** Privacy & data settings showing export data option, retention period display, and privacy policy link.

---

### 15g. Help

**Purpose:** In-settings support shortcuts (mirrors main Help tab).

#### Support Options

| Option | Description | Action |
|--------|-------------|--------|
| Help center | Guides for managing multiple locations | Visit link |
| Contact support | Direct line to Dinerly franchise support team | Contact button |

---

## 16. Help

**Navigation Path:** Sidebar → Help

**Overview:** Support resources for franchise owners.

#### Help Resources

| Resource | Description | Action |
|----------|-------------|--------|
| Help center | Guides for managing multiple locations | Visit link |
| Contact support | Direct line to Dinerly franchise support team | Contact link |
| Report a problem | Something not working as expected in the admin portal? | Report link |

**Screenshot Reference:** Help page showing three support options with descriptions and action links.

---

## 17. Dashboard Insight Content Reference

**Purpose:** Details on the 4 insight card types referenced in Section 1, for the team building the underlying data feed.

### Insight Card Types

#### Growth

**Purpose:** Identify growth opportunities

**Logic:** Flags locations without an active offer

**Data Elements:**
- Which location(s)
- Join-rate lift percentage seen at locations WITH active offers
- Link to Offers management for that location
- Link to create new offer

**Example:**
> "2 locations are leaving waitlist joins on the table"  
> "Osborne Village and Corylan Ave have no active offer and are missing your offer on their join rate at location offer see do reducing your offers & more body see the value for in-venue-offers"
> Button: "Create an offer"

---

#### Operations — No-Shows

**Purpose:** Flag operational issues

**Logic:** Surfaces locations with elevated no-show rate

**Data Elements:**
- Which location
- Current no-show percentage
- Comparison to acceptable threshold
- Link to hold-time settings for that location
- Action: "Review hold-time settings"

**Example:**
> "No-shows are quietly costing you tables"  
> "SG: Brooklyn's is losing out behind to lots the mix up -- and that's costing you. What's happening here is usually overbooking rather than wait time. But a longer hold time to pick up this last."
> Button: "Review hold-time settings"

---

#### Operations — Closed Location

**Purpose:** Flag scheduling issues

**Logic:** Flags a location that's closed with no scheduled reopen time

**Data Elements:**
- Which location
- Closed status and duration
- Link to Store availability for that location
- Action: "Schedule reopening" or "Review hold-time settings"

**Example:**
> "SB: Meridian is closed with no reopening time set"  
> "Let admin know a location goes offline without a scheduled reopen"
> Button: "Review hold-time settings"

---

#### Tips — Wait Time Accuracy

**Purpose:** Improve service quality

**Logic:** Flags a location where actual seated time is drifting from quoted wait time at check-in

**Data Elements:**
- Which location
- Expected vs. actual wait time difference (in minutes)
- Link to Performance tab to see wait-time trends for adjustment
- Action: "See improving time"

**Example:**
> "SB: Meridian is closed with no reopening time set"  
> "Settling this of different from what guests ate at check-in — wait time quoting time shows more accurate for guests"
> Button: "See improving time"

---

#### Announcements

**Purpose:** Communicate product updates

**Logic:** Product-level updates (e.g., new Rewards program tiers)

**Data Elements:**
- Feature name/title
- Brief description
- Link to relevant admin panel tab (e.g., Rewards program)
- No location specificity

**Example:**
> "New Rewards program feature launched"  
> "Configure your loyalty tier system and start earning redemptions today"
> Button: "View Rewards program"

---

### Insight Card Data Requirements

**Critical:** Each card's data (which location, what percentage/number) should be computed from live metrics rather than hardcoded, since the copy references specific figures.

| Insight Type | Data Source | Refresh Cadence |
|--------------|-------------|-----------------|
| Growth | Offer table + join rate analytics | Daily |
| Operations — no-shows | Waitlist analytics table | Daily |
| Operations — closed location | Location availability + schedule table | Real-time |
| Tips — wait time accuracy | Waitlist performance data | Daily |
| Announcements | Product update feed | Quarterly/as-needed |

---

## 18. Open Items for Engineering

### Priority Implementation Tasks

1. **Performance Tab Location Filtering**
   - Wire Performance tab KPIs/charts (all 3 sub-tabs) to actually filter by selected location
   - Currently shows shared demo data regardless of location selected
   - Affects: Waitlist, Reviews, Rewards & Offers sub-tabs
   - Implementation: Query by location_id parameter

2. **Location Details Modal Persistence**
   - Persist edits made in Location details modal to backend:
     - Nickname
     - Address
     - Seats
     - Phone
     - Manager/owner contact
     - Open toggle
   - Currently modal opens but edits don't persist
   - Create endpoint: PUT `/api/locations/{id}`

3. **Voice Message Persistence**
   - Persist default table-ready voice message text
   - Confirm scope: per-location or per-account-level?
   - Connect persisted text to outbound call system (Twilio integration)
   - Endpoint: POST/PUT `/api/settings/voice-message`

4. **Insight Card Thresholds**
   - Insight card thresholds should be computed, not hardcoded:
     - No-show percentage threshold
     - Wait-time drift tolerance (minutes)
     - Join-rate lift baseline
   - Define underlying metrics and refresh cadence
   - Consider database table: `insight_thresholds` with configurable values

5. **Menu Item Photo Upload**
   - Camera placeholder currently shows for items without photo
   - Confirm: Is photo upload in scope for v1?
   - If yes: Implement file upload, storage (S3?), and preview
   - Endpoint: POST `/api/menu-items/{id}/photo`

6. **Admin Role vs. Owner Data Model**
   - Clarify distinction between:
     - **Account-level Owner** - Role pill in Admin tab (account owner's role)
     - **Per-location Owner** - Owner name/email field added in Locations modal
   - Confirm whether "Owner" role in Admin tab should be account-wide only
   - Update data model and validation rules accordingly

### Database Schema Recommendations

```sql
-- Location fields (extend existing)
ALTER TABLE locations ADD COLUMN owner_name VARCHAR(255);
ALTER TABLE locations ADD COLUMN owner_email VARCHAR(255);
ALTER TABLE locations ADD COLUMN manager_email VARCHAR(255);

-- Voice message settings
CREATE TABLE voice_message_settings (
    id BIGSERIAL PRIMARY KEY,
    restaurant_id BIGINT NOT NULL,
    location_id BIGINT,
    message_text TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now(),
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(id),
    FOREIGN KEY (location_id) REFERENCES locations(id)
);

-- Insight thresholds
CREATE TABLE insight_thresholds (
    id BIGSERIAL PRIMARY KEY,
    restaurant_id BIGINT NOT NULL,
    no_show_percentage_threshold DECIMAL(5,2),
    wait_time_drift_tolerance_minutes INTEGER,
    join_rate_lift_baseline DECIMAL(5,2),
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now(),
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(id)
);
```

### API Endpoints to Implement

| Method | Endpoint | Purpose |
|--------|----------|---------|
| PUT | `/api/locations/{id}` | Update location details |
| POST | `/api/locations/{id}/photo` | Upload menu item photo |
| GET/POST/PUT | `/api/settings/voice-message` | Manage voice message template |
| GET/POST | `/api/settings/insight-thresholds` | Manage insight card thresholds |
| GET | `/api/performance/waitlist?locationId={id}` | Filtered waitlist performance |
| GET | `/api/performance/reviews?locationId={id}` | Filtered reviews performance |
| GET | `/api/performance/rewards?locationId={id}` | Filtered rewards performance |

---

## Architecture Notes

### Frontend State Management

- Location context/state for filtering Performance data
- Insight card refresh interval (recommend: 5-10 minute cadence)
- Modal persistence state for Location details

### Backend Considerations

- Index location_id on performance tables (waitlist, reviews, redemptions)
- Cache insight card data with TTL (Time-To-Live)
- Implement role-based access control (RBAC) for Owner role visibility
- Validate voice message merge fields `{guest_name}` and `{restaurant_name}`

---

## Document Metadata

- **Version:** 1.0
- **Last Updated:** 2026-09-20
- **Status:** Complete for development handoff
- **Source:** Dinerly Admin Portal Prototype (FuegoFox design team)
- **Prepared for:** Engineering development team

---

**End of Document**
