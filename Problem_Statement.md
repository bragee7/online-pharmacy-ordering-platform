# Problem Statement — Online Pharmacy Ordering Platform

## 1. Project Title

**Online Pharmacy Ordering Platform** — a web‑based system that enables customers to browse
and order medicines online under licensed‑pharmacist supervision, with a verified
prescription workflow, real inventory control, and role‑based administration.

## 2. Problem Statement

Buying medicines is a health‑critical activity, yet most small‑to‑medium retail pharmacies
run on phone calls and manual registers:

- **No online presence** — customers cannot browse availability, prices, or order remotely.
- **Unsafe offline shortcuts** — medicines that legally require a prescription are often
  sold without any verification, creating a public‑health risk.
- **Manual inventory** — stock is tracked in notebooks or spreadsheets; shortages and
  expired stock are discovered only when a customer asks.
- **No order visibility** — customers can neither see whether their prescription was
  accepted nor track their order once placed.
- **No role separation** — anyone behind the counter can change prices, edit stock, or
  approve prescriptions; there is no audit trail.
- **No data** — owners have no reportable data (sales, low‑stock, pending prescriptions) to
  plan procurement or staffing.

Existing generic e‑commerce platforms cannot be reused because pharmacy ordering has
**non‑negotiable domain rules**: prescription‑gated purchases, expiry‑aware inventory,
product availability semantics (quantity vs. reserved quantity), and legally meaningful
order states.

## 3. Existing System

- Walk‑in counter purchase with phone verification; no website or catalogue.
- Prescription decisions made verbally at the counter with no record.
- Spreadsheet/notebook stock counts; no low‑stock or expiry alerts.
- No order tracking, no payment record, no per‑role permissions, no audit history.
- No management reports.

**Drawbacks:** unsafe prescription handling, stock‑out surprises, no audit trail,
no remote ordering, and zero actionable business data.

## 4. Proposed System

A modular web application with three separate layers:

- **React frontend (customer, pharmacist and admin views)** that talks only to REST APIs.
- **Spring Boot backend** that owns every business rule (stock safety, prescription gates,
  server‑sided pricing, order state machine) and enforces authentication, authorization
  and ownership with JWT + Spring Security.
- **MySQL 8 persistence** built entirely through Flyway migrations with seed data.

The system digitises the full lifecycle of a purchase: browse → cart → prescription
verification → order → payment (mock, gateway‑ready) → tracking, with a complete audit
history written at every state change.

## 5. Objectives

1. Provide a secure online catalogue with search, filter and pagination.
2. Implement authenticated multi‑role access: `CUSTOMER`, `PHARMACIST`, `ADMIN`.
3. Enforce prescription verification **server‑side** before prescription‑required items
   can be ordered; record pharmacist approval/rejection and reasons.
4. Protect inventory against overselling using reservation semantics and locking.
5. Implement a validated order state machine with cancellation rules and a full status
   history.
6. Make the system payment‑ready through a swappable `PaymentGateway` abstraction.
7. Keep the codebase testable (JUnit 5 + Mockito), documented (Swagger/OpenAPI) and
   deployable from a clean machine (Maven Wrapper, Flyway).

## 6. Target Users and Roles

| Role | Target user | Access |
|---|---|---|
| CUSTOMER | Individuals ordering medicines | Own profile, addresses, cart, prescriptions, orders, payments (ownership enforced) |
| PHARMACIST | Licensed pharmacist | Review prescriptions, fulfil orders, monitor inventory & low stock |
| ADMIN | Pharmacy owner/administrator | Full catalogue, users, pharmacists, orders, reports, dashboard |

## 7. Core Features

- JWT authentication (register/login) with BCrypt‑hashed passwords
- Profile & address management with ownership checks
- Medicine catalogue: CRUD, name/generic/brand search, category/price/prescription filters, pagination
- Shopping cart: add/update/remove, server‑sided subtotal, stock + availability + prescription validation
- Prescription upload and status tracking (PENDING → APPROVED / REJECTED / EXPIRED)
- Order placement with tax/delivery/discount calculation, stock reservation, prescription
  gate, status history, cancellation and refund‑ready payment state
- Inventory: available vs reserved quantity, reorder level, low‑stock flagging, no negatives
- Mock payment gateway behind an interface (real gateways swappable later)
- Admin dashboard and reports backed by real database statistics

## 8. Business Rules and Workflows

- A medicine flagged `prescriptionRequired` **cannot** be ordered without a valid
  `APPROVED`, non‑expired prescription for that customer — validated in the backend.
- Inventory never goes negative; ordering uses a reserve→deduct flow with pessimistic
  locking to prevent concurrent overselling.
- Prices and totals are computed only from persisted data.
- Order status follows a strict machine: `PENDING → CONFIRMED → PACKED → SHIPPED →
  OUT_FOR_DELIVERY → DELIVERED`, with `PRESCRIPTION_PENDING`, `CANCELLED`, `REJECTED`
  as valid branches. Transitions outside the machine are rejected.
- Orders may be cancelled only from `PENDING`/`CONFIRMED`; cancellation releases reserved
  stock and marks payment `REFUNDED`.
- A customer cannot read or mutate another customer's records even by URL manipulation.

**Workflow A — Customer order:** register → login → search → cart → checkout → backend
validates stock, price and prescription → mock payment → order created → track state.

**Workflow B — Prescription verification:** customer uploads prescription (`PENDING`) →
pharmacist reviews → `APPROVED`/`REJECTED` (with reason) → customer sees updated status →
approved prescriptions unlock ordering of Rx medicines.

## 9. Technology and Integration Scope

**Stack:** Java 17, Spring Boot 3, Spring Security + JWT, Spring Data JPA, MySQL 8, Flyway,
Springdoc OpenAPI, JUnit 5 + Mockito; React 18, Vite, Axios, Bootstrap 5, React Router.

**Ready‑for‑integration seams (no keys hard‑coded):**
- `PaymentGateway` → Razorpay / Stripe
- `NotificationService` → Email / SMS
- `MedicineAssistantService` → AI assistant API
- Maps/geo for delivery addresses

## 10. AI Enhancement and Future Scope

- **AI medicine search** — natural‑language queries ("medicine for cold and fever") mapped
  onto categories/medicines via `MedicineAssistantService`.
- **Prescription OCR/AI** — extract medicine names from uploaded images into executable
  item suggestions that still require pharmacist verification (never automatic prescribing).
- **Inventory forecasting** — predict reorder needs from sales history.
- AI is always positioned as an **assistant**, not as a doctor or pharmacist authority.

Future technical items: real payment gateway, email/SMS notifications, delivery tracking,
mobile (PWA) packaging, reports export, RBAC hardening, and deployment automation.

---

*This application is deliberately more than a CRUD system: it encodes real pharmacy domain
rules (prescription gates, reservation‑based inventory, a legal‑grade order state machine)
in a secure, testable, documented architecture.*