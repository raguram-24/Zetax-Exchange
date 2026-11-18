# TASKS — Java side (zetax-common, zetax-engine, zetax-gateway, zetax-persistor, zetax-db)

Owner: Raguram · Created: 2026-11-15 · Re-ordered around flows: 2026-11-17 · Method: `start-java-step` skill

Each step = **one business concept + one Java/Spring concept + tested code**.
Every change goes through `java-approval-gate`. Every change → `DEV_LOG.md`; every finished step → `LEARNING_LOG.md`.
Agreed 2026-11-15: approve every change (same as Go) · Raguram types only the new, important code · every step also feeds `INTERVIEW.md` (Java/Spring interview prep).
Agreed 2026-11-17: **one flow at a time, end to end** (gateway → engine → persistor → Postgres). Each flow has its own document in `docs/flows/`; a step builds only its flow's contracts. Step numbers were re-assigned on 2026-11-17 (older log entries use the old numbers; mapping at the bottom).

Why this order: inside-out within a flow. Contracts first (everyone depends on them), then the pure-Java book (business rules without Spring/Kafka noise), concurrency theory right before the first threaded code, then the engine on Kafka, the DB schema, the persistor, the gateway, and an end-to-end test that proves the whole flow. Later flows reuse that skeleton and add their rules.

Status: `todo` · `doing` · `done` · `parked`

```
J0–J0b   Foundations                                  → build + domain model
J1–J10   Flow BF-02 · place a limit order (full flow)  → "POST → matched → rows in Postgres"
J11–J13  Flows BF-03 market · BF-04 cancel/modify · BF-08 admin
J14–J16  Invariants · replay/recovery · safe retries
J17      Benchmarks                                   → "numbers for the CV"
```

## Steps overview

| # | Step | Flow | Status | Done when | Blocked by |
|---|---|---|---|---|---|
| J0 | Maven multi-module check | — | done | `mvn verify` green; I can explain reactor order and the BOM | — |
| J0b | Domain ER (`docs/ER.md`), grows per flow | — | done | v0 drawn; each flow updates it | — |
| J1 | Contracts in `zetax-common` | BF-02 | done | Enums, `PlaceOrder`, `OrderAccepted`, `TradeExecuted` with guards; JSON + golden-file tests | — |
| J2 | Book order + `PriceLevel` | BF-02 | todo | Fill/invariant tests pass (`filled + remaining = qty`) | — |
| J3 | `OrderBook`: limit matching | BF-02 | todo | Price-time + maker-price tests pass; remainder rests; events with ids | — |
| J4 | Concurrency basics (engine threads) | BF-02 | todo | Theory + small demos understood; engine thread model written down | — |
| J5 | `EngineRunner` (threads + `assign`) | BF-02 | todo | One thread per partition; book created on first sight of a symbol; poison command never kills the thread | — |
| J6 | Engine → `trades` producer | BF-02 | todo | Events published; `acceptedAt` / `executedAt` = command's record timestamp; acks=all, idempotence | — |
| J7 | `zetax-db`: migrations | BF-02 | todo | Flyway Docker step creates instruments, users, orders, trades + dev seeds + 2 DB users/grants | — |
| J8 | `zetax-persistor` | BF-02 | todo | Testcontainers: events → rows; same event twice → one row; offsets committed after DB commit | — |
| J9 | Gateway order API | BF-02 | todo | `POST /api/orders` → read-only checks (400/401) → Kafka → `202 {order_…, RECEIVED}`; 503 if Kafka is down | — |
| J10 | BF-02 end-to-end | BF-02 | todo | Testcontainers: POST → engine → persistor → `SELECT` shows the order + trades | — |
| J11 | Market orders + price band | BF-03 | todo | `NO_LIQUIDITY`, `PRICE_BAND` tests pass end to end | Reference price before the first trade (Q5) |
| J12 | Cancel, self-trade prevention, modify | BF-04 | todo | Every BF-04 row + R-08 + R-09 has a test | — |
| J13 | Admin: instruments | BF-08 | todo | Add/update/halt/resume/delist through `commands` (D-20); read-only gateway path decided | Cancels during halt (Q6); TF-08 change (gateway can't write) |
| J14 | A5 worked example + randomized invariants | — | todo | A5 as a test; 100k seeded random orders keep all invariants | — |
| J15 | Replay + recovery (TF-06) | — | todo | Testcontainers: kill + restart → identical book, no duplicate effects | — |
| J16 | Safe retries (`clientOrderId`) | own flow | todo | A retry after a lost `202` returns the same order, never a second one | Design in its flow doc |
| J17 | Benchmarks | — | todo | JMH + HdrHistogram numbers with machine specs | Q-06 (which numbers) |
| — | WebSocket market data | — | parked | — | **D-22**: market data moves to the Go service? Decide first |

## Open questions
1. **D-22** (proposed in `docs/go/DECISIONS.md`): market data moves from gateway to Go. Record or reject.
2. ~~**Modify** command shape~~ → designed in its flow (J12), 2026-11-17
3. ~~**Event list** for symbols~~ → designed in its flow (J13), 2026-11-17
4. ~~**Tick size** format~~ → `long`, fixed 2-decimal scale, `tickSize` long in the same units (0.05 → `5`), valid iff `priceTicks % tickSize == 0` (BF-02 decision 4), 2026-11-17
5. **Reference price** for the band before a symbol's first trade (J11).
6. **Cancels during halt**: still a 💡 suggestion (reject) (J13).
7. **Q-06**: which CV numbers, on which machine (J17).
8. ~~**RECEIVED**~~ → stays as a status, set by the gateway's `202` (BF-02 decision 3), 2026-11-17

## Step details

### J0 — Maven multi-module check
- [x] `mvn verify` from the root; read the reactor order
- [x] Understand: parent vs aggregator, `dependencyManagement` vs `dependencies`, the Spring Boot BOM
- [x] Confirm `zetax-common` has no Spring dependency (D-21) — `dependency:tree`: only JUnit 6.0.3 (test), 2026-11-16

### J0b — Domain ER
- [x] v0: SYMBOL, ORDER, TRADE for BF-02 (`docs/ER.md`)
- [x] v0.1: SYMBOL → INSTRUMENT, `name` added
- [x] v0.2: USER (seeded until auth), user FKs on ORDER and TRADE
- [x] Explain-back + learning log entry (2026-11-16)
- [x] v0.3: BF-02 full-flow decisions (timestamps, prefixed ids, min/max qty, data access), 2026-11-17
- Later versions are done inside the flow steps: v1 in J11, v2 in J12, v3 in J13

### J1 — Contracts (`zetax-common`) · BF-02
Plan and decisions: `docs/flows/BF-02-place-limit-order.md`.
- [x] Settle open questions 2–4 first (2026-11-17)
- [x] Delete the skeleton enums + `Topics` + `package-info` (implementation from scratch, 2026-11-17; done 2026-11-18)
- [x] Enums: `Side`, `OrderType` (`LIMIT`), `OrderStatus` (`RECEIVED, OPEN, PARTIALLY_FILLED, FILLED`) with DB codes (decision 23)
- [x] `PlaceOrder` command record (sealed `EngineCommand`), compact-constructor guards (decision 19) — typed by Raguram, `PlaceOrderTest`
- [x] `OrderAccepted` (full order + `acceptedAt`), `TradeExecuted` (+ `executedAt`) records (sealed `EngineEvent`)
- [x] JSON type info via `jackson-annotations` (2.21, Boot 4.1 BOM; Jackson 3.1.4 for tests); golden-file tests in `golden/command/`, `golden/event/`

### J2 — Book order + `PriceLevel` · BF-02
- [ ] Engine's mutable order (distinct name, e.g. `BookOrder`): id, user, side, price ticks, qty, filled; `fill(qty)` guards the invariant
- [ ] `PriceLevel`: FIFO queue + total qty
- [ ] Tests first

### J3 — `OrderBook`: limit matching · BF-02
- [ ] Bids `TreeMap` descending, asks ascending; order index by id
- [ ] Match while crossing; trade at maker price; remainder rests (GTC)
- [ ] Events with ids from an `EventIds` sequence (`partition-offset-n`); `tradeId = "trade_" + eventId`

### J4 — Concurrency basics · BF-02
- [ ] Thread confinement, JMM / happens-before, `volatile`, safe publication — small demos in a scratch test
- [ ] Write the engine's thread model (who owns what, who reads metrics) in `LEARNING_LOG`

### J5 — `EngineRunner` · BF-02
- [ ] Topic names in `application.yml` (decision 25: `zetax.${ZETAX_ENV:local}.engine.commands-inbound` / `.events-outbound`) + `zetax.kafka.partitions=3` (replaces `zetax.engine.partitions`), guard test for 3
- [ ] `SmartLifecycle` bean; one `KafkaConsumer` per thread with `assign()`
- [ ] Poll loop: record → book (created on first sight of a symbol) → events; poison command → logged + skipped/rejected, continue
- [ ] Shutdown with `wakeup()`; thread joins

### J6 — Producer · BF-02
- [ ] `KafkaProducer` acks=all, idempotence on, key = symbol
- [ ] `acceptedAt` / `executedAt` from the command's record timestamp (never the clock)

### J7 — `zetax-db` migrations · BF-02
- [ ] Folder `zetax-db/` (SQL only); Flyway Docker image as a one-off step in `docker-compose.yml`, apps start after it
- [ ] `V1`…: `instruments` (tick, min/max qty), `users`, `orders`, `trades` with `created_at`/`updated_at`, `accepted_at`, `executed_at`
- [ ] Dev seeds (instruments ABC/XYZ, users) only in the dev/demo profile
- [ ] DB users: `zetax_persistor` (full), `zetax_gateway` (`SELECT` only)
- [ ] Remove Flyway from the gateway pom (approval)

### J8 — `zetax-persistor` · BF-02
- [ ] New Maven module (depends on `zetax-common` only); shared DB records in `zetax-common` (decision 21)
- [ ] Topic name in `application.yml` (`zetax.${ZETAX_ENV:local}.engine.events-outbound`)
- [ ] Consumer group on the events topic; batch; insert orders on `OrderAccepted` (status OPEN), trades `ON CONFLICT DO NOTHING`, update `filled_qty` + status; commit offsets after the DB commit
- [ ] Testcontainers Postgres (+ Kafka) (dependency → approval); same event twice → one row
- [ ] Same `TradeExecuted` twice → `filled_qty` added once (trade insert + both order updates in one transaction; updates only if the trade was new)
- [ ] `trades` user FKs; decide composite FK to orders + `CHECK (maker_user_id <> taker_user_id)` (ER v0.2)

### J9 — Gateway order API · BF-02
- [ ] `POST /api/orders` + `X-User-Id` (D-16); REST DTOs in the gateway
- [ ] Read-only checks: user exists (401), instrument exists + tick multiple + min/max qty (400)
- [ ] `orderId = "order_" + ULID` (library → approval)
- [ ] Topic name in `application.yml` (`zetax.${ZETAX_ENV:local}.engine.commands-inbound`)
- [ ] Produce `PlaceOrder` (key = symbol); `202 {orderId, RECEIVED}` after the ack; 503 when Kafka is down

### J10 — BF-02 end-to-end
- [ ] Testcontainers: Kafka + Postgres + migrations; gateway, engine, persistor running
- [ ] POST → rows in `orders` + `trades` with the right status, filled qty, maker price, timestamps
- [ ] Flow explain-back; BF-02 doc status → done

### J11 — Market orders + price band · BF-03
- [ ] Contracts: `OrderType.MARKET`, `OrderRejected`, reason codes (`NO_LIQUIDITY`, `PRICE_BAND`), `CANCELLED`/`REJECTED` states (decide the 0-filled → CANCELLED arrow); flow doc BF-03 + lifecycle version
- [ ] Market = IOC; empty book → accepted then cancelled `NO_LIQUIDITY`
- [ ] Band: limit outside band → rejected; market stops at band → rest cancelled `PRICE_BAND`

### J12 — Cancel, STP, modify · BF-04
- [ ] Contracts: `CancelOrder`, `OrderCancelled`, `CancelRejected`, modify command + events, reason codes (`SELF_TRADE_PREVENTED`, …), `CANCELLED` state; flow doc BF-04 + lifecycle version
- [ ] Cancel: own / filled / already cancelled / not owner / unknown
- [ ] STP: cancel taker remainder, maker stays
- [ ] Modify: reduce keeps priority; price change / increase loses it

### J13 — Admin: instruments · BF-08
- [ ] Contracts: symbol commands + events (`SymbolAdded`, `SymbolUpdateRejected`, halt/resume/delist), `SymbolStatus`; flow doc BF-08
- [ ] Decide the path with a read-only gateway (likely gateway → `AddSymbol` → engine → `SymbolAdded` → persistor saves); `X-Admin-Key`
- [ ] Add / update (tick size only with no open orders) / halt / resume / delist (only with no open orders); replaces "book created on first sight"

### J14 — Worked example + randomized invariants
- [ ] A5 "one day in the market" as one readable test
- [ ] Seeded random test: no crossed book, filled + remaining = qty, bought = sold, replay gives equal book

### J15 — Replay + recovery
- [ ] Find last published `sourceOffset`; silent replay to it; then live
- [ ] Testcontainers Kafka: kill + restart → identical book

### J16 — Safe retries (`clientOrderId`)
- [ ] Flow doc + design (where the "seen before?" check lives, given a read-only gateway)
- [ ] Learning session: idempotent order entry

### J17 — Benchmarks
- [ ] JMH for `OrderBook`; HdrHistogram end-to-end; record machine specs

## Parked ideas
- Snapshots for fast restart · Kafka transactions comparison (B5 note)
- Redis cache for gateway reference data (instruments, users): only if benchmarks show a need (2026-11-17)
- Minikube: run ZetaX on local Kubernetes (Deployments for gateway/persistor, StatefulSet-like engine, Kafka via an operator such as Strimzi), after the first flows run on docker-compose (2026-11-17)
- Maven Enforcer `bannedDependencies` on `zetax-common` (fail the build on any `org.springframework`) + Maven Wrapper (`mvnw`) — new plugin/files, needs approval (from J0)

## Step number mapping (before → after 2026-11-17)
J1 → J1 · J2 → J2 · J3 → J3 · J4 → J11 · J5 → J12 · J6 + J13 → J13 · J7 → J14 · J8 → J4 · J9 → J5 · J10 → J6 · J11 → J15 · J12 → J9 · J14 → J8 · J15 → J17 · new: J7, J10, J16
