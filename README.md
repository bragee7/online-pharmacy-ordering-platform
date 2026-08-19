# MediCare — Online Pharmacy Ordering Platform

Spring Boot 3.2.5 / Java 17 REST API for browsing medicines, cart, checkout (COD),
prescriptions, inventory, payments and deliveries. Vanilla JS frontend is served
statically; PostgreSQL is the primary DB (H2 for tests); Flyway manages migrations.

## Features

- JWT auth (register/login/me), BCrypt passwords, role-based access
- Medicine catalog: search by keyword/category/Rx, pagination + sorting, soft-delete/restore
- Categories CRUD (admin)
- Cart: add/update/remove/clear, stock-checked
- Checkout: stock + Rx validation, order number generation, COD payment (PENDING),
  delivery (PENDING, +5 days), inventory decrement, cart clear, audit log
- Order state machine + cancel with restock/refund
- Prescriptions: upload, approve/reject (pharmacist/admin)
- Inventory: per-medicine stock, low-stock query (`quantity <= reorderLevel`)
- Dashboard/admin stats, audit log
- OpenAPI/Swagger UI, file uploads (prescription images, medicine images)
- Tests: Mockito unit + `@DataJpaTest` + `JwtUtil` tests (H2)
- Docker + docker-compose (postgres:16-alpine + app)

## Tech Stack

| Layer | Choice |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2.5 (web, data-jpa, security, validation) |
| DB | PostgreSQL 16 (runtime), H2 (test) |
| Migrations | Flyway (`classpath:db/migration`, `validate` in prod, disabled in tests) |
| Auth | jjwt 0.12.6 (HS256), `JwtUtil` / `JwtProperties` |
| Docs | springdoc-openapi 2.5.0 (`/api-docs`, `/swagger-ui.html`) |
| Utils | Lombok, Spring Security Test |
| Build | Maven (no `mvnw` wrapper in repo — install Maven, see below) |
| Containers | eclipse-temurin:17-jre-alpine, postgres:16-alpine |

## Architecture

```
                +------------------+
                |  Browser / SPA   |
                | (static /*.html) |
                +--------+---------+
                         |  REST /api/** (JWT Bearer)
                         v
                +--------+---------+
                | SecurityConfig   |
                | JwtAuthFilter    |
                +--------+---------+
                         |
        +----------------+------------------+
        |                |                   |
   AuthController  Medicine/Cart/Order  Admin/Dashboard
   AuthService     CartService          CategoryService
                   OrderService         InventoryService
                   MedicineService      PrescriptionService
                   PaymentService       DeliveryService
                   FileStorageService   AuditService
        +----------------+------------------+
                         | JPA
                         v
                +--------+---------+
                | PostgreSQL       |
                | (Flyway)         |
                +------------------+
```

Package layout: `controller`, `service`, `repository`, `entity`, `dto`,
`security` (`JwtUtil`, `JwtProperties`, `SecurityConfig`, `JwtAuthFilter`),
`enums` (`Role`, `OrderStatus`, `PaymentStatus`, `DeliveryStatus`, `PrescriptionStatus`),
`exception` (`BadRequestException`, `ResourceNotFoundException`, `InsufficientStockException`),
`config` (`WebConfig`, `OpenApiConfig`), `util` (`OrderNumberGenerator`).

> Note: only `AuthController` is currently implemented in `controller/`. The API
> table below documents the intended/currently-secured contract per
> `SecurityConfig` + services. Add controllers to match before relying on them.

## Prerequisites

- Java 17 (`java -v`)
- Maven 3.9+ (`mvn -v`) — **required** (no `mvnw`/`mvnw.cmd` in this repo)
- Docker + Docker Compose (for `docker compose up`)
- PostgreSQL 16 if running locally without Docker

## Quickstart

### Option A — Docker Compose (recommended)

```bash
# 1. Build jar
mvn -DskipTests package

# 2. Start postgres + app
docker compose up --build

# 3. Open
# API:  http://localhost:8080
# Swagger: http://localhost:8080/swagger-ui.html
# Health:  http://localhost:8080/health
```

Compose wires `SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/pharmacy_db`,
`JWT_SECRET`/`JWT_EXPIRATION` (overridable via `.env` — copy `.env.example`).

### Option B — Local (Postgres)

```bash
createdb pharmacy_db
# edit src/main/resources/application.properties or export:
# SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD
mvn spring-boot:run
```

### Option C — Tests only (H2, no Postgres needed)

```bash
mvn test
```

Uses `src/test/resources/application-test.properties`
(`jdbc:h2:mem:testdb`, `ddl-auto=create-drop`, `flyway.enabled=false`).

## Demo Credentials

Seeded via migration/manual insert (if migrations present):

| Role | Email | Password |
|---|---|---|
| ADMIN | admin@medicare.com | password |
| PHARMACIST | pharmacist@medicare.com | password |
| CUSTOMER | customer@medicare.com | password |

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"customer@medicare.com","password":"password"}'
```

## API Endpoints

Base: `/api`. Auth: `Authorization: Bearer <jwt>` unless `permitAll`.

| Method | Path | Roles | Notes |
|---|---|---|---|
| POST | /api/auth/register | permitAll | `{name,email,password,phone?,address?,role?}` — only CUSTOMER allowed; creates User + Cart, returns `{token,tokenType,user}` |
| POST | /api/auth/login | permitAll | `{email,password}`; 400 on bad creds/disabled |
| GET | /api/auth/me | authenticated | current `UserDto` |
| GET | /api/medicines?keyword=&categoryId=&rx=&page=&size=&sort= | permitAll | `MedicineService.search`; includes `stock` (available) |
| GET | /api/medicines/{id} | permitAll | `getById` |
| POST | /api/medicines | ADMIN (+PHARMACIST per policy) | `MedicineRequest{name,price,categoryId?,initialStock,reorderLevel,...}` |
| PUT | /api/medicines/{id} | ADMIN | `update` |
| PATCH | /api/medicines/{id}/delete | ADMIN | soft-delete (`active=false`) |
| PATCH | /api/medicines/{id}/restore | ADMIN | `active=true` |
| GET | /api/categories | permitAll | list |
| POST | /api/categories | ADMIN | create |
| GET | /api/cart | authenticated | `getCart(userId from JWT)` |
| POST | /api/cart/items?medicineId=&qty= | authenticated | `addItem`, stock-checked |
| PUT | /api/cart/items/{itemId}?qty= | authenticated | `updateItem` (0 = delete) |
| DELETE | /api/cart/items/{itemId} | authenticated | `removeItem` |
| POST | /api/orders/checkout | authenticated | `{shippingAddress,paymentMethod=COD,prescriptionIds?}` → Order + COD PENDING Payment + PENDING Delivery |
| GET | /api/orders | authenticated | own orders (paged) |
| GET | /api/orders/{id} | owner/STAFF | `getById` |
| GET | /api/orders/number/{orderNumber} | owner/STAFF | `getByOrderNumber` |
| PATCH | /api/orders/{id}/cancel | CUSTOMER,ADMIN | owner or staff; only PLACED/CONFIRMED; restocks |
| PATCH | /api/orders/{id}/status?status= | ADMIN/PHARMACIST* | `updateStatus`; DELIVERED/CANCELLED immutable |
| GET | /api/orders/all?status= | ADMIN/PHARMACIST | `listAll` |
| POST | /api/prescriptions (multipart) | authenticated | upload Rx file |
| GET | /api/prescriptions | authenticated | own; staff sees all/filtered |
| PUT | /api/prescriptions/{id} | PHARMACIST,ADMIN | approve/reject |
| GET | /api/inventory/{medicineId} | ADMIN/PHARMACIST | `getByMedicine` |
| PUT | /api/inventory/{medicineId} | ADMIN/PHARMACIST | `update(quantity,reorderLevel)` |
| GET | /api/inventory/low-stock | ADMIN/PHARMACIST | `lowStock()` |
| GET | /api/admin/dashboard | ADMIN | `DashboardService` stats |
| GET | /api-docs, /swagger-ui.html, /health, /uploads/** | permitAll | docs/health/static |

\* `SecurityConfig` currently maps `PATCH /api/orders/**` to `CUSTOMER,ADMIN` and
`PUT /api/prescriptions/**` to `PHARMACIST,ADMIN`. Adjust `@PreAuthorize` as needed.

## RBAC Matrix

| Capability | CUSTOMER | PHARMACIST | ADMIN |
|---|---|---|---|
| Register/login, browse medicines/categories | ✅ | ✅ | ✅ |
| Cart + checkout + own orders + cancel own PLACED/CONFIRMED | ✅ | ❌* | ✅ (staff override) |
| Upload prescription | ✅ | ✅ | ✅ |
| Approve/reject prescription (`PUT /api/prescriptions/**`) | ❌ | ✅ | ✅ |
| Create/update medicines, inventory, low-stock | ❌ | read-only† | ✅ |
| All orders, update status, dashboard (`/api/admin/**`) | ❌ | partial‡ | ✅ |

\* Pharmacists typically don't order; enforced by convention, not filter.
† Give pharmacists write if desired via method security.
‡ `SecurityConfig`: `GET /api/orders/**` = authenticated; add staff checks in service.

## Order State Machine

```
PLACED → CONFIRMED → PACKED → SHIPPED → OUT_FOR_DELIVERY → DELIVERED
  |          |
  +--> CANCELLED <--+  (only from PLACED/CONFIRMED; restocks + refunds SUCCESS payment)
```

- `updateStatus` rejects transitions from `DELIVERED`/`CANCELLED`.
- `DELIVERED` marks linked `Delivery` DELIVERED.
- `CANCELLED` restocks `Inventory.quantity += OrderItem.quantity`.

## Payment / Delivery Notes

- Checkout `paymentMethod` defaults to `COD`. COD → `Payment.status=PENDING`,
  non-COD → `SUCCESS` + `paidAt=now`. `transactionReference` generated.
- Every order creates `Delivery{trackingNumber, carrier="MediCare Logistics",
  status=PENDING, estimatedDeliveryDate=+5d}`.
- Cancel refunds only `SUCCESS` → `REFUNDED`; COD PENDING stays PENDING.
- Rx rule: `medicine.prescriptionRequired=true` requires ≥1 APPROVED prescription
  for the user, else 400. Non-Rx medicines checkout without Rx.
- Stock rule: `Inventory.getAvailable() = quantity - reservedQuantity` must be
  `>= CartItem.quantity`, else `InsufficientStockException` (400).

## Testing

```bash
mvn test
```

| Test | Type | Covers |
|---|---|---|
| `AuthServiceTest` | Mockito `@InjectMocks` | register ok, duplicate email 400, login bad-creds 400 |
| `MedicineServiceTest` | Mockito | `create` ok, `search` page + stock mapping |
| `OrderServiceTest` | Mockito (lenient) | `checkout` ok (total, decrement, cart clear), empty cart 400 |
| `InventoryLowStockTest` | `@DataJpaTest` + H2 + `@ActiveProfiles("test")` | `findLowStock` (`quantity <= reorderLevel`) |
| `JwtUtilTest` | plain unit (manual `JwtProperties`) | generate/extract/valid, wrong-user invalid |

## Project Structure

```
.
├── Dockerfile
├── docker-compose.yml
├── .dockerignore
├── .env.example
├── LICENSE
├── README.md
├── pom.xml (Spring Boot 3.2.5, Java 17, jjwt 0.12.6, springdoc 2.5.0)
├── uploads/
├── src/main/java/com/medicare/pharmacy/
│   ├── PharmacyApplication.java
│   ├── controller/AuthController.java
│   ├── service/{Auth,Medicine,Cart,Order,Inventory,Category,Prescription,Payment,Delivery,Dashboard,Audit,FileStorage}Service.java
│   ├── repository/{User,Medicine,Category,Inventory,Cart,CartItem,Order,OrderItem,Payment,Delivery,Prescription,AuditLog}Repository.java
│   ├── entity/{User,Medicine,Category,Inventory,Cart,CartItem,Order,OrderItem,Payment,Delivery,Prescription,AuditLog}.java
│   ├── dto/{RegisterRequest,LoginRequest,AuthResponse,UserDto,MedicineRequest,MedicineDto,CategoryDto,CartDto,CartItemDto,CheckoutRequest,OrderDto,OrderItemDto,PrescriptionDto,PaymentDto,DeliveryDto,InventoryDto,DashboardStats,ApiResponse}.java
│   ├── security/{SecurityConfig,JwtUtil,JwtProperties,JwtAuthFilter,CustomUserDetailsService,AuthEntryPoint,AccessDeniedHandlerImpl}.java
│   ├── enums/{Role,OrderStatus,PaymentStatus,DeliveryStatus,PrescriptionStatus}.java
│   ├── exception/{BadRequestException,ResourceNotFoundException,InsufficientStockException,GlobalExceptionHandler}.java
│   ├── config/{WebConfig,OpenApiConfig}.java
│   └── util/OrderNumberGenerator.java
├── src/main/resources/application.properties
└── src/test/{java/com/medicare/pharmacy/{AuthServiceTest,MedicineServiceTest,OrderServiceTest,InventoryLowStockTest,JwtUtilTest}.java,resources/application-test.properties}
```

## Configuration

| Key | Default | Env override (compose) | Notes |
|---|---|---|---|
| `server.port` | 8080 | `PORT` | `EXPOSE 8080` |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/pharmacy_db` | `SPRING_DATASOURCE_URL` | compose: `...://postgres:5432/pharmacy_db` |
| `spring.datasource.username/password` | postgres/postgres | `SPRING_DATASOURCE_USERNAME/PASSWORD` | — |
| `spring.jpa.hibernate.ddl-auto` | validate | — | tests use `create-drop` |
| `spring.flyway.enabled/locations` | true / `classpath:db/migration` | — | tests disable; add `V1__init.sql` (currently missing) |
| `jwt.secret` | 55-char capstone secret | `JWT_SECRET` | must be ≥32 bytes for HS256; tests use 64+ chars |
| `jwt.expiration` | 86400000 | `JWT_EXPIRATION` | ms |
| `app.upload.dir` | uploads | — | mounted `./uploads:/app/uploads` in compose |
| `springdoc.api-docs.path/swagger-ui.path` | /api-docs, /swagger-ui.html | — | — |

## Troubleshooting

- `mvn` not found → install Maven 3.9+ (no wrapper committed). `mvn -v` must work.
- Flyway `validate` fails / missing `db/migration` → create `src/main/resources/db/migration/V1__init.sql` or set `ddl-auto=update` locally only.
- `JWT secret too weak` (jjwt) → use 32+ bytes (see `.env.example`).
- `Connection refused postgres` → `docker compose up postgres`, check `5432`, `pgdata` volume.
- `Port 8080 in use` → change `server.port`/`PORT` or stop conflicting app.
- Tests hit Postgres instead of H2 → ensure `@ActiveProfiles("test")` + `spring.flyway.enabled=false`.
- `uploads` permission errors → `mkdir uploads`, check `app.upload.dir`, compose volume.
- `401 on /api/medicines POST` → needs ADMIN JWT; `GET` is public, writes are not.

## License

MIT © 2026 MediCare — see [LICENSE](LICENSE).
