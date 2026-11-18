# TASKS — what we are going to do

Project: **ZetaX Market Data** (Go learning project) · Owner: Raguram · Created: 2026-11-10

## The big picture

We build **one ZetaX service in Go**, step by step: the market-data read side.
Each step adds one Go idea. Steps 0–8 run on a **fake event source**, so nothing waits for the Java engine.
Real Kafka comes in Step 9, once the engine (or a hand-typed test event) can put events on the `trades` topic.

```
Step 0–2   setup, event types, fake events      → "it understands ZetaX events"
Step 3–5   book view, candles, wiring           → "it keeps live market state"
Step 6–8   REST, WebSocket, shutdown            → "browsers can watch the market"
Step 9–11  Kafka, my orders, Postgres           → "it's part of the real ZetaX"
Step 12–13 metrics, Docker, benchmarks          → "it's production-shaped"
```

How we work (see `CLAUDE.md`):

- Claude Code **never writes code without Raguram's approval**.
- Every step: learn the idea (`LEARNING.md`) → plan → propose code → approve → write → run → explain back → log.
- Every change → `DEV_LOG.md`. Every finished step → `LEARNING_LOG.md`.

Status values: `todo` · `doing` · `done` · `parked`

## Before Step 0 — business reading (Raguram, ~2 days)

No code. Read these in the vault (`wiki/projects/order-matching-engine/`) so the Go steps make sense:

- [ ] `Order Matching Engine - Business and Technical Flow.md` → **Start here** (order, book, limit/market, partial fill, spread, tick, price-time priority)
- [ ] Same page → **Part A**: A3 order lifecycle, A4 business flows (BF-01…BF-08), A5 one day in the market, A6 business rules
- [ ] Same page → **B2** Kafka topics + message shapes, **TF-04** live market data, **B5** recovery / exactly-once
- [ ] `Order Matching Engine - Overview.md` → decisions D-08, D-11, D-13, D-14, D-16, D-18, D-21
- [ ] Write down questions for `DECISIONS.md` (open questions section)
- [ ] Explain back to Claude Code: "what does the market-data service receive, what does it keep, what does it send?"

## Steps overview

| # | Step | Status | Done when |
|---|---|---|---|
| 0 | Setup | todo | `go run .` prints hello; module created; repo on GitHub; VS Code Go extension works |
| 1 | Event types + JSON decoding | todo | Sample events from `testdata/` decode into Go structs; unknown type → clear error; tests pass |
| 2 | Fake event source | todo | A deterministic generator produces a realistic event stream for 2 symbols |
| 3 | Book view (price levels) | todo | Applying events gives correct bid/ask levels; duplicates ignored; table-driven tests pass |
| 4 | Trades + OHLC candles | todo | 1-minute candles built from event time; tests cover boundaries and gaps |
| 5 | Wire it together | todo | `main` runs: fake source → channel → processor; state logged with `slog`; config from env |
| 6 | REST API | todo | `GET` book snapshot / recent trades / candles work; `httptest` tests; `go test -race` clean |
| 7 | WebSocket hub | todo | Many clients subscribe per symbol; snapshot + deltas with sequence numbers; slow clients dropped |
| 8 | Graceful shutdown + timeouts | todo | Ctrl+C stops every goroutine cleanly; server timeouts set; no leaks in tests |
| 9 | Real Kafka consumer | todo | Events from the `trades` topic reach the processor; one goroutine per partition; replay on start |
| 10 | My orders per user | todo | A user's WebSocket gets only their own order updates (`?userId=`, D-16) |
| 11 | Postgres candle history | todo | Closed candles saved; history endpoint reads them; migrations run |
| 12 | Metrics, health, Docker | todo | `/healthz`, Prometheus metrics, small multi-stage image, runs in ZetaX docker compose |
| 13 | Benchmarks + hardening | todo | Benchmarks, a fuzz test for the decoder, `pprof` profile, N-client WebSocket load test |
| 14 | (Optional) Demo bot / load generator | parked | Go CLI places random orders through the gateway and reports latency (feeds ZetaX Q-06) |

## Step details

### Step 0 — Setup
Raguram runs these himself (Claude Code only explains):

- [ ] `brew install go` → `go version` (note the version in DEV_LOG)
- [ ] VS Code: install the official Go extension; let it install `gopls` and tools
- [ ] Git lives at the **repo root** (`zetax-exchange/`): `git init` there once if it isn't a repo yet (no `git init` inside this folder)
- [ ] In this folder: `go mod init github.com/raguram-24/zetax-exchange/zetax-marketdata` (confirm module path first)
- [ ] First `main.go` printing a hello line; `go run .`, `go build`, `go vet ./...`
- [ ] Understand: module vs package, `package main`, `go.mod`, why there is no `src/main/java`
- [ ] `cp .env.example .env` (not used until Step 5)
- [ ] First commit `chore(step-00): marketdata project setup` (only this folder's files); push with the rest of `zetax-exchange`

### Step 1 — Event types + JSON decoding
- [ ] Agree the v1 event shapes in `EVENTS.md` (open items listed there)
- [ ] `testdata/events/*.json`: one sample file per event type (copied from `EVENTS.md`)
- [ ] Go structs with JSON tags for each event; `Side`, `EventType` as typed string constants
- [ ] Decode by `type` field (read the type first, then decode the rest)
- [ ] Errors: unknown type, missing field, bad JSON → wrapped errors, no panics
- [ ] First table-driven test with `go test`

### Step 2 — Fake event source
- [ ] An `EventSource` interface (defined where it's used) + `FakeSource` implementation
- [ ] Deterministic: seeded random, event time advances by a fixed step (no `time.Now()`)
- [ ] Produces accepted / trade / cancelled events that make sense together for 2 symbols
- [ ] Prints events as JSON lines (`go run ./cmd/fakegen` or a flag — decide in the step)

### Step 3 — Book view (price levels)
- [ ] `Book` per symbol: bids and asks as price → total qty
- [ ] Apply rules per event type (from `EVENTS.md`); level removed when qty hits 0
- [ ] Sorted snapshot: bids high→low, asks low→high, top N levels
- [ ] Idempotency: ignore an `eventId` already applied
- [ ] Per-symbol sequence number increases on every change
- [ ] Table-driven tests for each rule, duplicates and empty book

### Step 4 — Trades + OHLC candles
- [ ] Recent trades ring (last N per symbol)
- [ ] 1-minute candles: open, high, low, close, volume; bucket by **event time**
- [ ] Minute with no trades → decide (skip or flat candle) and record in DECISIONS
- [ ] Tests at bucket boundaries

### Step 5 — Wire it together
- [ ] `Processor`: the **only** goroutine that changes market state; reads events from a channel
- [ ] `main`: load config from env, create source + processor, start, log with `slog`
- [ ] Understand: goroutine, unbuffered vs buffered channel, who closes a channel

### Step 6 — REST API
- [ ] `GET /api/marketdata/{symbol}/book`, `/trades`, `/candles?interval=1m` (paths to confirm with the web repo)
- [ ] Safe reads while the processor writes (mutex vs ask-the-processor via channel — compare both, pick one)
- [ ] JSON errors (404 unknown symbol, 400 bad interval)
- [ ] `httptest` tests; `go test -race ./...`

### Step 7 — WebSocket hub
- [ ] Choose WebSocket library + message protocol (MD-Q01, MD-Q02)
- [ ] Hub: subscribe / unsubscribe per symbol channel (`book.ABC`, `trades.ABC`)
- [ ] On subscribe: snapshot with its sequence number, then deltas
- [ ] One writer goroutine per client with a buffered send channel; full buffer → disconnect (slow client)
- [ ] Ping/pong + read deadline to detect dead clients
- [ ] Test with 2 real clients (e.g. `websocat`) + a Go test

### Step 8 — Graceful shutdown + timeouts
- [ ] `signal.NotifyContext` for Ctrl+C / SIGTERM
- [ ] `http.Server` with read/write/idle timeouts; `Shutdown(ctx)`
- [ ] Every goroutine stops on context cancel; `WaitGroup` (or `errgroup`) to wait for them
- [ ] Test that shows no goroutine is left running

### Step 9 — Real Kafka consumer
- [ ] Choose Kafka client (MD-Q03)
- [ ] `KafkaSource` implements the same `EventSource` interface (fake stays for tests)
- [ ] One goroutine per partition; ordering per symbol kept
- [ ] Start-up: replay from the earliest offset to rebuild state (MD-Q04)
- [ ] Try it without the engine: `kafka-console-producer` with a sample event from `testdata/`
- [ ] (Optional) integration test with Testcontainers for Go

### Step 10 — My orders per user
- [ ] Per-user channel: order accepted / filled / cancelled / rejected for that user only
- [ ] User from `?userId=` on connect (D-16); JWT later

### Step 11 — Postgres candle history
- [ ] Choose driver + migration tool (MD-Q05); own schema or table prefix (MD-Q06)
- [ ] Save each closed candle (upsert, idempotent)
- [ ] `GET .../candles?from=&to=` reads history
- [ ] Connection pool settings understood

### Step 12 — Metrics, health, Docker
- [ ] `/healthz` (alive) and `/readyz` (Kafka caught up)
- [ ] Metrics: events/sec, consumer lag, WebSocket clients, dropped slow clients
- [ ] Multi-stage Dockerfile (static binary, small base image)
- [ ] Add the service to the root `docker-compose.yml` (repo-root change: needs approval)

### Step 13 — Benchmarks + hardening
- [ ] `testing.B` benchmark for applying events to a book
- [ ] Fuzz test for the event decoder (`go test -fuzz`)
- [ ] `pprof` CPU + memory profile under load
- [ ] Load test: N WebSocket clients, measure fan-out latency

## Parked ideas

- STOMP over WebSocket (only if the web client must stay on STOMP — MD-Q02)
- Book snapshots so restart doesn't depend on topic retention (MD-Q04)
- Kubernetes deploy, gRPC streaming API, more candle intervals (5m, 1h)
