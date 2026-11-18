# ZetaX Exchange — rules for Claude Code

## What this is
An order matching engine (mini exchange). Full design lives in Raguram's Obsidian vault:
`~/Documents/Second_Brain/wiki/projects/order-matching-engine/`
- `Order Matching Engine - Overview.md` → decisions D-01…D-21 (authoritative)
- `Order Matching Engine - Business and Technical Flow.md` → business rules, flows, topics, schema
Read the relevant section before building a feature. **Never invent requirements**: if something isn't decided, ask.
**Flows:** the final design of each flow (diagrams + decisions) lives in the repo, one document per flow in `docs/flows/`; there the repo wins over the vault's planning diagrams (decided 2026-11-17).

## Stack
Java 21 · Spring Boot 4 · Maven multi-module · Kafka (single broker, KRaft) · PostgreSQL 16 + JDBC (`JdbcClient`, no JPA) · Flyway (migrations in `zetax-db/`, run by the Flyway Docker image) · Testcontainers · STOMP WebSocket · React + TS later (phase 3b).

## Repo layout (one repo, several apps)
| Folder | What | Build |
|---|---|---|
| `zetax-common`, `zetax-engine`, `zetax-gateway`, `zetax-persistor` | Java modules (below) | Maven, root `pom.xml` |
| `zetax-db/` | Postgres schema: Flyway SQL migrations (`V1__…sql`), dev seeds, DB users + grants. SQL only | Flyway Docker image (one-off step in `docker-compose.yml`) |
| `zetax-marketdata/` | **Go** market-data service (port 8082). Has its own `CLAUDE.md`, docs and skills — read them when working there; its Go rules win over the Java parts of this file | `go` (own `go.mod`, not a Maven module) |
| `zetax-web/` | React + TS trading screen. Has its own `CLAUDE.md` | `npm` |

**Docs:** `docs/` = the exchange, shared by all apps (`flows/`, `ER.md`, `GLOSSARY.md`, `PROGRESS_BUSINESS_LEARNING.md`); `docs/java/` = Java learning track; `docs/go/` = Go learning track (for `zetax-marketdata`).

**Private:** all learning files (`LEARNING*.md`, `DEV_LOG.md`, `INTERVIEW.md`, `PROGRESS_BUSINESS_LEARNING.md`, `GLOSSARY.md`) are git-ignored and stay local; never `git add -f` them. The repo is public: design, plan, code, `CLAUDE.md` and skills.

The ZetaX-wide rules below (ticks, determinism, `eventId`, Kafka keys, never invent requirements, ask before dependencies) apply to **every** folder. Stack and "Done means" for Java apply to the Java modules only.

## Modules (D-21, extended 2026-11-17: `zetax-persistor` added)
- `zetax-common`: plain Java (records/enums). **No Spring, no JPA.** Holds what **more than one app** uses: Kafka messages, enums, shared DB data records. Anything used by one app stays in that app (e.g. REST DTOs in the gateway, the engine's mutable book order).
- `zetax-engine`: stateful. `book` = pure Java matching (no Spring, no Kafka). `runner` = threads + Kafka. `journal` = Journal interface.
- `zetax-gateway`: stateless. `api.order`, `api.admin` (market data: parked, D-22).
- `zetax-persistor`: reads `trades` events (consumer group) and saves orders + trades to Postgres.
- Apps talk **only through Kafka**. Never call each other directly.

## Data access
- `zetax-persistor` does **all** writes (insert / update / delete). `zetax-gateway` is **read-only** (`instruments`, `users`) with a `SELECT`-only DB user. `zetax-engine` **never** touches the DB.
- Schema changes only as new Flyway files in `zetax-db/`; never edit an applied migration.
- Business time = the command's Kafka record timestamp (`accepted_at`, `executed_at`); `created_at` / `updated_at` = DB save time.
- Ids: `orderId = "order_" + ULID` (gateway); `tradeId = "trade_" + partition-offset-n`; `eventId = partition-offset-n`.

## Hard rules
- Money/prices: `long` ticks. Quantities: `long`. **Never `double`/`float`.**
- Kafka: both topics keyed by **symbol**; `commands` has 3 partitions, fixed (D-18). Producers: `acks=all`, idempotence on.
- Engine consumers use `consumer.assign()`, **never** `subscribe()` (no rebalancing of in-memory books).
- Engine is **deterministic**: no `System.currentTimeMillis()`, no random, no DB reads in matching. Time = Kafka record timestamp.
- Every event has a deterministic `eventId` = `partition-offset-n`; consumers must be idempotent.
- Symbol changes go through the commands topic too (D-20), not only the DB.
- Business rules (D-13): price-time priority; trade at resting (maker) price; market = immediate-or-cancel;
  self-trade prevention cancels the taker remainder; reduce-qty keeps priority, price change/qty increase loses it;
  per-symbol min/max qty and price band; limit orders GTC.
- REST answers `202 Accepted` after the Kafka ack; results go over WebSocket (D-14). User = `X-User-Id` header (D-16).
- No `@Transactional`, `@Async` or other proxies in the matching code.
- Ask before adding any dependency.
- Log messages start with `<methodName> -> `; the class (Java, `%logger{0}`) or type (Go, `slog.With("type", …)`) is added automatically. Never caller-data / stack-walking log patterns.

## Working style
- Small steps: one class / one rule at a time, with tests. Plan first (files, classes), wait for OK.
- Raguram is a strong Java/Spring dev learning trading systems: explain trading/Kafka concepts briefly when they first appear.
- Tests: JUnit 5 for `book` (every business rule + a randomized invariant test); Testcontainers for Kafka/Postgres flows.

## Learning mode (Java side)
Raguram learns the **business** and **Java / Spring / concurrency** concepts while building, same method as the Go service.
- Plan and status: `docs/java/TASKS.md` (steps J0–J17, grouped by flow). Flow designs: `docs/flows/`. What each step teaches: `docs/java/LEARNING.md`.
- Every step: business concept → Java concept → plan → approved pieces (≤ ~30 lines) → explain-back → logs (`start-java-step`).
- **No change without approval** (`java-approval-gate`); log every change in `docs/java/DEV_LOG.md`, every finished step in `docs/java/LEARNING_LOG.md`.
- Raguram types **only the new, important code** (matching loop, concurrency, key Spring wiring); routine code Claude writes after approval. Review his code, don't rewrite it.
- This is also his **Java / Spring Boot interview prep**: every concept gets the interview question, a 30-second answer, the deeper answer, the ZetaX story and the trap → `docs/java/INTERVIEW.md`.

## Skills (`.claude/skills/`)
| Skill | Use when |
|---|---|
| `java-unit-tests` | Writing, updating or reviewing any Java test (book rules, randomized invariants, Spring slices, Testcontainers, regression tests) |
| `java-clean-code` | Writing, refactoring or reviewing Java code: SOLID, DRY, KISS/YAGNI, no new Sonar issues, review checklist before a commit |
| `design-flow` | Designing a flow before any code ("design flow BF-03", a step whose flow has no document yet): scope, decisions one at a time, contracts, diagrams, data model, build plan, explain-back |
| `start-java-step` | "start step Jn", "next Java step", "what's next" |
| `java-approval-gate` | Before ANY Java-side change: code, pom, config, migration, docs, dependency, commit |
| `explain-java-concepts` | Teaching a Java / JVM / concurrency / Kafka-client / Spring concept |
| `teach-business-concept` | Teaching a trading concept (step start, "what does X mean", business sessions) |

The Go service has its own skills in `zetax-marketdata/.claude/skills/`.

## Done means
`mvn verify` passes, no new warnings, no new SonarQube for IDE issues on changed files (checked after **every function**, not only at the end), new behaviour covered by tests (`java-unit-tests`), `java-clean-code` checklist done.
