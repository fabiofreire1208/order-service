# Load Tests

Simple [Grafana k6](https://k6.io/) load tests that validate the main Order Service
business flows. The goal is to confirm the application behaves correctly under a
light, sustained load — not to run stress tests.

---

## Scripts

| Script            | Flow                                              | Validates                                             |
| ----------------- | ------------------------------------------------- | ----------------------------------------------------- |
| `create-order.js` | `POST /api/v1/orders`                             | Order accepted (`202`), duplicates (`200`)            |
| `get-order.js`    | `GET /api/v1/orders/{externalOrderId}`            | Order retrieved (`200`) and returned correctly        |
| `mixed-flow.js`   | `POST` → wait for processing → `GET`              | End-to-end flow, order reaches status `CALCULATED`    |

Each execution generates a unique `externalOrderId`, so the scripts can be run
repeatedly against the same database without collisions.

---

## Load Profile

All scripts share a simple profile:

- Ramp up to 10 virtual users (30s)
- Hold the load (1m)
- Ramp down to 0 (30s)

## Thresholds

The test fails if either threshold is breached:

- `http_req_failed` — less than 5% of requests may fail
- `http_req_duration` — 95% of requests must complete under 1000 ms

---

## Prerequisites

- The Order Service must be running and reachable (see the root [README](../README.md)).
  The simplest way is `docker compose up` from the project root.
- Grafana k6 installed locally, **or** Docker to run k6 in a container.

---

## Installation

Install k6 following the [official instructions](https://grafana.com/docs/k6/latest/set-up/install-k6/):

```bash
# macOS
brew install k6

# Debian / Ubuntu
sudo gpg -k
sudo gpg --no-default-keyring --keyring /usr/share/keyrings/k6-archive-keyring.gpg --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
sudo apt-get update
sudo apt-get install k6
```

No installation is required if you run k6 through Docker (see below).

---

## Execution

Run any script from the `load-tests` directory.

### Using a local k6

```bash
k6 run create-order.js
k6 run get-order.js
k6 run mixed-flow.js
```

### Using Docker

```bash
docker run --rm -i grafana/k6 run - <create-order.js
```

> On Linux, the container reaches the host via `http://localhost:8080` only when
> run with `--network host`. Otherwise point `BASE_URL` at
> `http://host.docker.internal:8080`:
>
> ```bash
> docker run --rm -i -e BASE_URL=http://host.docker.internal:8080 \
>   grafana/k6 run - <create-order.js
> ```

---

## Environment Variables

| Variable                  | Default                 | Used by         | Description                                              |
| ------------------------- | ----------------------- | --------------- | -------------------------------------------------------- |
| `BASE_URL`                | `http://localhost:8080` | all scripts     | Base URL of the running Order Service.                   |
| `PROCESSING_WAIT_SECONDS` | `3`                     | `mixed-flow.js` | Seconds to wait for asynchronous processing before GET.  |

Example:

```bash
k6 run -e BASE_URL=http://localhost:8080 -e PROCESSING_WAIT_SECONDS=5 mixed-flow.js
```

---

## Scope

These tests are intentionally kept simple. Distributed execution, Grafana
dashboards, InfluxDB, Prometheus and cloud execution are out of scope for this
challenge.
