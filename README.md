# Online Pharmacy Ordering Platform

A production‑style **B.E. Computer Science & Engineering capstone project**: a complete
online pharmacy ordering system with real pharmacy business logic, three user roles,
prescription verification, inventory management, order processing, payment‑ready
architecture and a modern React frontend.

> Backend: **Java 17 · Spring Boot 3 · Spring Security + JWT · JPA/Hibernate · MySQL 8 · Flyway**
> Frontend: **React 18 · Vite · Bootstrap 5 · Axios · React Router**

---

## Project Overview

The platform lets customers browse and order medicines online, upload prescriptions that
licensed pharmacists review and approve/reject, and track orders through a validated state
machine. A pharmacist manages prescriptions, order fulfilment and inventory warnings. An
admin manages the entire catalogue, users and the business via a real data dashboard. The
backend enforces every business rule — stock safety, prescription gates, pricing and order
state transitions are **never** trusted to the frontend.

## Features

- JWT authentication with BCrypt password hashing (3 roles: `CUSTOMER`, `PHARMACIST`, `ADMIN`)
- Role‑based authorization + per‑resource ownership checks
- Medicine catalogue with search, multi‑filter, and pagination (`/api/medicines/search`)
- Prescription upload + pharmacist approval/rejection workflow (with rejection reason)
- Shopping cart with server‑sided pricing, stock validation and prescription validation
- Order processing with full state machine, cancellation rules, and status history
- Inventory with reserve / release / deduct semantics and pessimistic locking (no oversell)
- Mock payment gateway behind a `PaymentGateway` interface (Razorpay/Stripe can be plugged in)
- Admin dashboard with real statistics from the database
- Swagger/OpenAPI documentation
- Extensibility seams for email/SMS, mapping, and AI assistant services

## User Roles

| Role        | Capabilities |
|-------------|--------------|
| **CUSTOMER**   | Register, manage profile & addresses, browse/search medicines, cart, upload prescriptions, place/track/cancel orders |
| **PHARMACIST** | Review pending prescriptions (approve/reject with reason), fulfil orders (status updates), monitor inventory & low stock |
| **ADMIN**      | Manage users, medicines, categories, inventory; view all orders/prescriptions; dashboard statistics & reports |

## Technology Stack

**Backend** — Java 17, Spring Boot 3.x, Spring Web, Spring Data JPA (Hibernate), Spring Security,
JJWT, Bean Validation, Lombok, Flyway, MySQL 8, Springdoc OpenAPI, JUnit 5 + Mockito.

**Frontend** — React 18, Vite, Axios, Bootstrap 5, React Router, JSX.

## Architecture

```
Customer ─▶ React (Vite) ──axios/JWT──▶ Spring Boot REST ──JPA/Flyway──▶ MySQL 8
                                          │
                                          ├── PaymentGateway (mock)   ──▶ future Razorpay/Stripe
                                          ├── NotificationService      ──▶ future Email/SMS
                                          └── MedicineAssistantService ──▶ future AI
```

See [`docs/diagrams/architecture.md`](docs/diagrams/architecture.md),
[`docs/diagrams/er-diagram.md`](docs/diagrams/er-diagram.md) and
[`docs/diagrams/class-diagram.md`](docs/diagrams/class-diagram.md).

## Database

14 related tables managed by Flyway migrations (`backend/src/main/resources/db/migration`):
`roles`, `users`, `addresses`, `categories`, `medicines`, `inventory`, `prescriptions`,
`prescription_items`, `carts`, `cart_items`, `orders`, `order_items`, `payments`,
`order_status_history`. Schema is created entirely via migrations on first boot.

## Prerequisites

- **Java 17+** (Java 21 works; bytecode targets 17)
- **Node.js 18+** and npm
- **MySQL 8** running locally (the app uses the `online_pharmacy` database)
- No system Maven required — a committed **Maven Wrapper** (`mvnw.cmd`) downloads Maven itself.

## Installation

### 1. Clone / open the repository

```bash
git clone <repo-url> online-pharmacy-ordering-platform
cd online-pharmacy-ordering-platform
```

### 2. Configure environment variables

Copy the example and fill in values:

```bash
# backend/
cp ../.env.example .env        # or set OS env vars
```

Required values: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` (≥ 32 chars).

### 3. Database setup

Create the MySQL database and user (or let the app create the database if
`createDatabaseIfNotExist=true`):

```sql
CREATE DATABASE online_pharmacy CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'pharmacy'@'localhost' IDENTIFIED BY 'your-password';
GRANT ALL PRIVILEGES ON online_pharmacy.* TO 'pharmacy'@'localhost';
FLUSH PRIVILEGES;
```

On first boot, Flyway creates all tables and seed scripts load sample
categories, medicines and inventory. Users are seeded by the backend using BCrypt.

### 4. Backend setup

```bash
cd backend
./mvnw spring-boot:run
# or, on Windows:
mvnw.cmd spring-boot:run
```

The API is served at `http://localhost:8081`. Swagger UI: `http://localhost:8081/swagger-ui.html`
(or `/swagger-ui/index.html`).

### 5. Frontend setup

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`. The Vite dev server proxies `/api` to the backend.

## Database Setup (without MySQL installed)

If you have no MySQL server, see `scripts/` for a portable local instance:
`scripts/db-start.cmd` / `scripts/db-stop.cmd` initialise and run a MySQL 8 data
directory placed outside the repository. See `scripts/README.md`.

## Running the Application

| Component | Command | URL |
|---|---|---|
| Backend  | `cd backend && mvnw.cmd spring-boot:run` | http://localhost:8081 |
| Swagger  | — | http://localhost:8081/swagger-ui/index.html |
| Frontend | `cd frontend && npm install && npm run dev` | http://localhost:5173 |

## API Documentation

OpenAPI 3 is enabled via springdoc. Browse: <http://localhost:8081/swagger-ui/index.html>
(or `/swagger-ui.html`). Interactive API is documented per module: auth, users, categories,
medicines, inventory, cart, prescriptions, orders, payments, admin.

## Test Instructions

```bash
cd backend
mvnw.cmd test
```

Unit/integration tests cover authentication, duplicate email, invalid credentials,
medicine CRUD/search/pagination, cart operations, insufficient stock, prescription
approve/reject, order creation/cancellation, invalid status transitions, ownership and
role restrictions. Test configuration uses the `online_pharmacy_test` schema.
See [`docs/api`](docs/api) for endpoint walkthroughs.

## Demo Credentials

These are created at startup when `APP_SEED_ENABLED=true` (dev/demo only):

| Role | Email | Password |
|---|---|---|
| Admin | `admin@example.com` | `Admin@123` |
| Pharmacist | `pharmacist@example.com` | `Pharmacist@123` |
| Customer 1 | `customer@example.com` | `Customer@123` |
| Customer 2 | `customer2@example.com` | `Customer@123` |

## Project Structure

```
online-pharmacy-ordering-platform/
├── backend/            Spring Boot application (Java 17, Maven Wrapper)
├── frontend/           React 18 + Vite application
├── docs/
│   ├── diagrams/       architecture, ER, class diagrams (Mermaid)
│   ├── api/            API documentation & walkthroughs
│   └── database/       schema notes
├── scripts/            local MySQL helper scripts
├── .env.example        environment template
├── Problem_Statement.md
└── README.md
```

## Business Workflows

**Customer order flow:** register → login → search medicines → add to cart → checkout
(server validates stock, pricing and prescriptions) → mock payment → order created →
track order (state machine). See [`docs/diagrams/architecture.md`](docs/diagrams/architecture.md).

**Prescription verification flow:** customer uploads prescription (PENDING) → pharmacist
reviews → APPROVED / REJECTED (with reason) → customer sees updated status → approved
prescriptions enable ordering prescription‑required medicines.

## AI Scope

Designed so AI can be added without refactoring:

- **`MedicineAssistantService`** — interface for natural‑language medicine search
  (default: deterministic keyword/category matcher, no API key needed).
- **Prescription OCR** — prescriptions already store item extracts; an OCR/AI stage can
  populate them, with pharmacist verification (never automatic prescribing).
- The AI is an **assistant**, never the doctor/pharmacist authority.

## Future Enhancements

- Real payment gateway (Razorpay/Stripe) via `PaymentGateway`
- Email/SMS notifications via `NotificationService`
- Address lookup / delivery tracking via maps APIs
- Stock alerts, reports export, chat‑based refill assistant

## Community / Support

- **Contribute:** see [`CONTRIBUTING.md`](CONTRIBUTING.md) (Conventional Commits required)
- **Problem statement:** see [`Problem_Statement.md`](Problem_Statement.md)

## License

[MIT](LICENSE)