# Loan Management System (Loan API)

A REST API to manage the end-to-end lifecycle of personal loan applications (creation, retrieval, status transitions, searching and auditing). The project follows **Hexagonal Architecture (Ports & Adapters / Clean Architecture)** on **Spring Boot 3 / Java 21**, using **Oracle** as the database and **Hibernate Envers** for full auditability.

The API is also deployed on a personal server and can be accessed at:
- **Base URL (prod)**: `http://ssh.alvarodelaflor.com:8080`

When using Postman:
- Use **environment `CAIXABANKTECH-PROD`** (in `docs/postman/`) against the deployed server.
- Use **environment `CAIXABANKTECH-LOCAL`** for running the API locally (Docker or direct Spring Boot).

## Demo

![Demo](docs/media/demo.gif)

## Quick start
- **100% test coverage achieved**
- Start everything (Oracle + API): `docker compose up --build`
- Swagger UI (local): http://localhost:8080/swagger-ui/index.html#/
- Swagger UI (prod): http://ssh.alvarodelaflor.com:8080/swagger-ui/index.html#/
- Postman collection to run the full flow end-to-end: `docs/postman/CAIXABANKTECH.postman_collection.json`

![Swagger](docs/media/swagger.jpeg)
> **Disclaimer**: depending on the browser and security settings, requests to non-HTTPS URLs (like `http://localhost:8080` or `http://ssh.alvarodelaflor.com:8080`) may be blocked for embedded content such as Swagger UI. If Swagger does not load correctly, try using a different browser, adjusting mixed-content settings, or accessing the API directly via Postman/cURL.

---

## 1) Running the project

### Prerequisites
- **Docker + Docker Compose** (recommended)
- **Java 21 + Maven 3.9+** (only if you want to run the app outside Docker)

### Docker (recommended)

#### Oracle + API (main environment)
Start Oracle (container) and the API:

- Normal start:
  - `docker compose -f docker-compose.yaml up --build`

- Clean start (removes previous DB volume/state):
  - `docker compose -f docker-compose.yaml down -v --remove-orphans && docker compose -f docker-compose.yaml up --build`

Notes:
- First boot can take ~1–2 minutes while Oracle initializes and Flyway runs migrations.
- `docker-compose.yaml` exposes:
  - API on **8080**
  - Java debug port on **8081**

#### Raspberry Pi / lightweight environment (H2 in Oracle compatibility mode)
To run the same API on a Raspberry Pi (or any low-resource environment) without starting Oracle, a second compose file is provided that uses **H2 in Oracle compatibility mode** embedded in the application:

- File: `docker-compose-raspberry.yaml`
- Services:
  - `app` (container `loan-api-h2`)
    - Exposes:
      - API on **8080**
      - Debug port on **8081**
    - Important env vars:
      - `SPRING_PROFILES_ACTIVE=h2-oracle` → activates the profile defined in `application.yml` that configures H2 to behave like Oracle.

How to start on a Raspberry Pi (or any host with Docker):

- Normal start:
  - `docker compose -f docker-compose-raspberry.yaml up --build`

Notes:
- No external database container is started: H2 runs embedded inside the API container.
- The `h2-oracle` profile in `application.yml` configures:
  - H2 URL: `jdbc:h2:./data/loan-db;MODE=Oracle;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;AUTO_SERVER=TRUE`
  - Driver: `org.h2.Driver`
  - `spring.jpa.hibernate.ddl-auto=update`
  - `spring.flyway.enabled=false` (Flyway migrations are designed for a real Oracle database).

---

## 2) API access & documentation

- **Base URL (local)**: `http://localhost:8080/api/v1/loans`
- **Base URL (prod)**: `http://ssh.alvarodelaflor.com:8080/api/v1/loans`
- **Swagger UI (local)**: http://localhost:8080/swagger-ui.html
- **Swagger UI (prod)**: http://ssh.alvarodelaflor.com:8080/swagger-ui.html
- **OpenAPI UI (local)**: http://localhost:8080/swagger-ui/index.html

### Main endpoints
- `POST /api/v1/loans` — create a loan application (initial status `PENDING`)
- `GET /api/v1/loans/{id}` — retrieve by UUID
- `PATCH /api/v1/loans/{id}/status` — status transition (`APPROVED`, `REJECTED`, `CANCELLED`)
- `GET /api/v1/loans/{id}/history` — audit/history (Envers)
- `GET /api/v1/loans/search/{applicantIdentity}` — search by DNI/NIE
- `GET /api/v1/loans/search/criteria` — search by optional filters (DNI/NIE and/or date range)
- `DELETE /api/v1/loans/{id}` — delete a loan application by UUID

Errors:
- The API returns errors following **RFC 7807 (Problem Details)**.

---

## 3) Postman collection (run everything end-to-end)

Under `docs/postman/` you have:
- **Collection**: `CAIXABANKTECH.postman_collection.json`
- **Local environment**: `CAIXABANKTECH-LOCAL.postman_environment.json`
- **Prod environment**: `CAIXABANKTECH-PROD.postman_environment.json`

The collection is designed to run the full workflow:
1. **POST Create Loan**
2. **GET Retrieve Loan by ID**
3. **GET Retrieve Loan by ApplicantIdentity**
4. **GET Retrieve Loan by Criteria**
5. **PATCH Approve / Reject / Cancel**
6. **GET Loan History**

The first request (**Create Loan**) includes a test script that automatically stores these environment values for subsequent requests:
- `id`
- `applicantIdentity`
- `createdAt`
- `modifiedAt`

### How to use
1. Import the **collection** and the **environment** in Postman.
2. Select:
   - `CAIXABANKTECH-LOCAL` to hit `http://localhost:8080`.
   - `CAIXABANKTECH-PROD` to hit `http://ssh.alvarodelaflor.com:8080`.
3. Ensure the `caixabanktech-url` variable matches the chosen environment (including scheme, e.g. `http://ssh.alvarodelaflor.com:8080`).
4. Run the collection using the Postman **Runner** to execute all requests in order.

---

## 4) Architecture & technical decisions

### Hexagonal Architecture
- **Domain**: `LoanApplication` aggregate + Value Objects (e.g., `ApplicantIdentity`, `LoanAmount`) enforcing invariants and valid state transitions.
- **Application**: use cases (inbound ports) such as `CreateLoanUseCase`, `RetrieveLoanUseCase`, `ModifyLoanStatusUseCase`.
- **Infrastructure**: REST adapter (Spring MVC) and persistence adapter (Spring Data JPA + Oracle).

### Key decisions
- **Oracle container** to be closer to a real banking-like environment.
- **Hibernate Envers** for a complete chronological audit trail.
- **Flyway** for schema versioning.
- **MapStruct** for efficient, compile-time DTO ↔ domain mapping.

---

## 5) Testing (100% coverage)
- Unit tests on the domain (invariants/state transitions).
- MVC/controller tests to validate HTTP contracts.

---

## 6) Possible improvements
- Security: OAuth2/JWT (client vs manager roles).
- Observability: Micrometer/Prometheus.
- Caching: Redis.
- Async processing/events: Kafka.

---

### Disclaimer
*Developed by @alvarodelaflor*
