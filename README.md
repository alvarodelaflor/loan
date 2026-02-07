# Loan Management System (Loan API)

A REST API to manage the end-to-end lifecycle of personal loan applications (creation, retrieval, status transitions, searching and auditing). The project follows **Hexagonal Architecture (Ports & Adapters / Clean Architecture)** on **Spring Boot 3 / Java 21**, using **Oracle** as the database and **Hibernate Envers** for full auditability.

## Demo

![Demo](docs/media/demo.gif)

## Quick start
- **100% test coverage achieved**
- Start everything (Oracle + API): `docker compose up --build`
- Swagger UI: http://localhost:8080/swagger-ui.html
- Postman collection to run the full flow end-to-end: `docs/postman/CAIXABANKTECH.postman_collection.json`

---

## 1) Running the project

### Prerequisites
- **Docker + Docker Compose** (recommended)
- **Java 21 + Maven 3.9+** (only if you want to run the app outside Docker)

### Docker (recommended)
Start Oracle (container) and the API:

- Normal start:
  - `docker-compose up --build`

- Clean start (removes previous DB volume/state):
  - `docker-compose down -v --remove-orphans && docker-compose up --build`

Notes:
- First boot can take ~1–2 minutes while Oracle initializes and Flyway runs migrations.
- `docker-compose.yaml` exposes:
  - API on **8080**
  - Java debug port on **8081**

---

## 2) API access & documentation

- **Base URL**: `http://localhost:8080/api/v1/loans`
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI UI**: http://localhost:8080/swagger-ui/index.html

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
2. Select the environment `CAIXABANKTECH-LOCAL`.
3. Ensure `caixabanktech-url` includes the scheme, e.g.:
   - `http://localhost:8080`
   (If you set it to `localhost:8080` without `http://`, Postman can fail to build the URL depending on settings.)
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
- Integration tests with **Testcontainers** (real Oracle) to validate SQL dialect compatibility.
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
