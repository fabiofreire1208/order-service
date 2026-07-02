# Order Service

A backend service responsible for receiving customer orders, calculating totals asynchronously and exposing processed orders for consultation.

Built with Java 21 and Spring Boot 3, following Hexagonal Architecture and modern backend engineering practices.

---

## Table of Contents

- [Business Overview](#business-overview)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Architectural Decisions](#architectural-decisions)
- [Running Locally](#running-locally)
- [API Reference](#api-reference)
- [Future Improvements](#future-improvements)

---

## Business Overview

The Order Service acts as an intermediary between two external systems:

- **External System A** — sends customer orders via REST.
- **External System B** — queries processed orders via REST.

### Business Flow

```
External System A
      ↓
POST /api/v1/orders
      ↓
Persist Order (status: RECEIVED)
      ↓
Save Outbox Event (same transaction)
      ↓
Scheduled Publisher → RabbitMQ
      ↓
OrderReceivedConsumer
      ↓
Calculate Order Total
      ↓
Update Order (status: CALCULATED)
      ↓
External System B queries GET /api/v1/orders/{externalOrderId}
```

Order ingestion is decoupled from order processing. The API responds immediately after persisting the order, while calculation happens asynchronously in the background.

---

## Architecture

The project follows **Hexagonal Architecture (Ports & Adapters)**.

Business rules live in the Domain layer and have zero dependency on frameworks. The Application layer orchestrates use cases through interfaces (Ports). The Infrastructure layer provides concrete implementations of those interfaces.

```
Infrastructure  →  Application  →  Domain
```

Dependencies always point inward. The Domain knows nothing about Spring, JPA, RabbitMQ or HTTP.

### Layers

| Layer | Responsibility |
|---|---|
| **Domain** | Business entities, business rules, domain exceptions |
| **Application** | Use cases, commands, input/output ports |
| **Infrastructure** | Controllers, JPA repositories, RabbitMQ, persistence adapters |

---

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 21 | Language |
| Spring Boot 3 | Application framework |
| Maven | Build tool |
| PostgreSQL | Primary data store |
| RabbitMQ | Asynchronous messaging |
| Spring Data JPA | Persistence abstraction |
| Spring AMQP | Messaging abstraction |
| Flyway | Database migrations |
| MapStruct | Object mapping |
| Lombok | Boilerplate reduction |
| Bean Validation | Input validation |
| SpringDoc OpenAPI | API documentation |
| Docker Compose | Local environment |
| JUnit 5 + Mockito + MockMvc | Testing |
| Grafana k6 | Load testing |

---

## Project Structure

```
src/main/java/com/fabiofreire/orderservice
├── domain
│   ├── model           # Order, OrderItem, OrderStatus
│   └── exception       # Domain exceptions
├── application
│   ├── command         # CreateOrderCommand, OrderItemCommand
│   ├── port
│   │   ├── in          # Input ports (use case interfaces) + result types
│   │   └── out         # Output ports (repository + outbox interfaces)
│   └── service         # Use case implementations
├── infrastructure
│   ├── adapter
│   │   ├── in
│   │   │   ├── web        # REST controllers, DTOs, mappers, exception handler
│   │   │   └── messaging  # RabbitMQ consumer
│   │   └── out
│   │       ├── entity     # JPA entities
│   │       ├── mapper     # Entity ↔ domain mapper
│   │       ├── messaging  # Outbox event publisher
│   │       └── repository # Spring Data JPA repositories
│   └── config          # RabbitMQ and scheduling configuration
└── shared              # Shared message contracts
```

---

## Architectural Decisions

### Why Hexagonal Architecture?

Hexagonal Architecture isolates business rules from infrastructure concerns. Controllers, JPA entities and RabbitMQ are implementation details — the domain never depends on them. This makes the domain easy to unit test without a Spring context, and allows infrastructure components to be replaced independently without touching business logic.

### Why PostgreSQL?

PostgreSQL provides strong consistency, reliable transactions and unique constraint enforcement. The `externalOrderId` uniqueness guarantee is enforced at the database level, ensuring idempotency even under concurrent requests. The expected workload of 150,000–200,000 orders per day is well within PostgreSQL's capacity with proper indexing.

### Why RabbitMQ?

Order calculation is decoupled from order ingestion through asynchronous messaging. This allows the API to respond quickly after persisting the order, while processing happens independently. RabbitMQ provides a reliable transport with Dead Letter Queue support for failed messages, and the architecture allows future scaling by adding more consumers without changing the producer side.

### Why the Outbox Pattern?

Without the Outbox Pattern, persisting an order and publishing a RabbitMQ message are two separate operations. If the application crashes after saving the order but before publishing the event, the message is lost. The Outbox Pattern solves this by saving the event to the database within the same transaction as the order. A scheduled publisher then reads pending events and publishes them to RabbitMQ, guaranteeing at-least-once delivery.

### Why the Command Pattern?

Write operations are expressed as Commands (`CreateOrderCommand`) that carry all the data needed to execute a use case. Controllers create commands and delegate execution to use case interfaces. This keeps controllers thin and concentrates business orchestration in the Application layer.

---

## Running Locally

### Prerequisites

- Docker
- Docker Compose

### Start

```bash
docker compose up --build
```

This starts:
- PostgreSQL on port `5432`
- RabbitMQ on ports `5672` (AMQP) and `15672` (Management UI)
- Order Service on port `8080`

The application waits for PostgreSQL and RabbitMQ to be healthy before starting.

### Swagger UI

```
http://localhost:8080/swagger-ui/index.html
```

### RabbitMQ Management

```
http://localhost:15672
```

Credentials: `orderservice / orderservice`

### Stop

```bash
docker compose down
```

To also remove volumes:

```bash
docker compose down -v
```

---

## API Reference

### Create Order

```http
POST /api/v1/orders
Content-Type: application/json
```

**Request body:**

```json
{
  "externalOrderId": "ORDER-10001",
  "customerId": "CUSTOMER-001",
  "items": [
    {
      "productId": "PRODUCT-01",
      "quantity": 2,
      "unitPrice": 25.50
    },
    {
      "productId": "PRODUCT-02",
      "quantity": 1,
      "unitPrice": 100.00
    }
  ]
}
```

**Response — new order (`202 Accepted`):**

```json
{
  "externalOrderId": "ORDER-10001",
  "status": "RECEIVED",
  "message": "Order accepted for processing."
}
```

**Response — duplicate order (`200 OK`):**

```json
{
  "externalOrderId": "ORDER-10001",
  "status": "CALCULATED",
  "message": "Order already exists."
}
```

**cURL example:**

```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "externalOrderId": "ORDER-10001",
    "customerId": "CUSTOMER-001",
    "items": [
      { "productId": "PRODUCT-01", "quantity": 2, "unitPrice": 25.50 },
      { "productId": "PRODUCT-02", "quantity": 1, "unitPrice": 100.00 }
    ]
  }'
```

---

### Get Order

```http
GET /api/v1/orders/{externalOrderId}
```

**Response (`200 OK`):**

```json
{
  "externalOrderId": "ORDER-10001",
  "customerId": "CUSTOMER-001",
  "status": "CALCULATED",
  "totalAmount": 151.00,
  "items": [
    {
      "productId": "PRODUCT-01",
      "quantity": 2,
      "unitPrice": 25.50,
      "totalAmount": 51.00
    },
    {
      "productId": "PRODUCT-02",
      "quantity": 1,
      "unitPrice": 100.00,
      "totalAmount": 100.00
    }
  ],
  "createdAt": "2026-06-28T18:00:00Z",
  "updatedAt": "2026-06-28T18:00:05Z"
}
```

**cURL example:**

```bash
curl http://localhost:8080/api/v1/orders/ORDER-10001
```

---

### List Orders

```http
GET /api/v1/orders?status=CALCULATED&customerId=CUSTOMER-001&page=0&size=20
```

All query parameters are optional.

**Response (`200 OK`):**

```json
{
  "content": [
    {
      "externalOrderId": "ORDER-10001",
      "customerId": "CUSTOMER-001",
      "status": "CALCULATED",
      "totalAmount": 151.00
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

**cURL example:**

```bash
curl "http://localhost:8080/api/v1/orders?status=CALCULATED&page=0&size=20"
```

---

### HTTP Status Codes

| Status | Description |
|---|---|
| `200 OK` | Successful request |
| `202 Accepted` | Order accepted for processing |
| `400 Bad Request` | Validation or business rule violation |
| `404 Not Found` | Order not found |
| `500 Internal Server Error` | Unexpected error |

---

### Error Response

```json
{
  "timestamp": "2026-06-28T18:00:00Z",
  "status": 404,
  "message": "Order not found: ORDER-99999",
  "path": "/api/v1/orders/ORDER-99999"
}
```

---

## Future Improvements

The following topics were intentionally left out of scope but could be addressed in a production environment:

- **Authentication and Authorization** — Secure the API with OAuth2/JWT.
- **Distributed Tracing** — Add OpenTelemetry for request tracing across services.
- **Metrics and Monitoring** — Expose Prometheus metrics and build Grafana dashboards.
- **Retry and Dead Letter Queue handling** — Implement retry policies for failed message processing and a dedicated DLQ consumer for alerting.
- **Testcontainers** — Add integration tests using real PostgreSQL and RabbitMQ containers to complement the existing unit tests.
- **Outbox at scale** — Replace the polling-based Outbox publisher with a change data capture (CDC) approach using tools like Debezium for higher throughput.
- **Horizontal scaling** — The current in-process scheduler requires a distributed lock (e.g. ShedLock) when running multiple instances to prevent duplicate event publishing.
- **API pagination hardening** — Enforce maximum page size limits to prevent large result set queries.
