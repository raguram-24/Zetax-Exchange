# CLAUDE.md — ZetaX Market Data (Go learning project)

Owner: Raguram — backend developer (Java, Spring Boot, TypeScript, NestJS, Docker, Kubernetes, AWS), **new to Go**.

Goal: learn Go by building **one real ZetaX component**, the market-data service, step by step.
Learning comes first. Working code is the by-product.
Raguram is **not** reading Go lessons first: you teach each Go concept **just in time**, when a build step needs it.

## What this service is

`zetax-marketdata` is the **read side** of ZetaX (CQRS). It never talks to the engine directly.

```
Java engine ──► Kafka "trades" topic ──► zetax-marketdata (Go) ──WebSocket──► React screen
                                              │  └─REST (snapshots, candles)──►
                                              └──► PostgreSQL (candle history, later)
```

- Reads engine events from the Kafka `trades` topic (keyed by symbol, 3 partitions).
- Keeps, per symbol: a **price-level book view** (total qty per price), recent trades, **OHLC candles**.
- Pushes **snapshot + deltas with sequence numbers** over WebSocket; REST for snapshots and history.

This folder lives **inside the `zetax-exchange` repo** (MD-04): the Java modules are `../zetax-common`, `../zetax-engine`, `../zetax-gateway`; the React screen is `../zetax-web`.

**Two CLAUDE.md files apply here.** The repo-root `../CLAUDE.md` holds the ZetaX-wide rules (ticks, determinism, `eventId`, Kafka keys, never invent requirements). Its Java/Maven parts (Java 21, Spring Boot, `mvn verify`, JUnit) do **not** apply to this Go folder. If the two files ever disagree, this file wins for code in `zetax-marketdata/`.
ZetaX design (business rules, flows, topics) lives in Raguram's vault:
`~/Documents/Second_Brain/wiki/projects/order-matching-engine/` (Overview = decisions D-01…D-21; "Business and Technical Flow" = TF-04 market data, B2 topics + message shapes).
Read the relevant section before building a feature. **Never invent requirements or event fields.** If something isn't decided, ask.

**Shared with the Java side** (the exchange itself, repo root `../docs/`): flow designs `../docs/flows/` (one document per flow), domain model `../docs/ER.md`, trading terms `../docs/GLOSSARY.md`, business learning `../docs/PROGRESS_BUSINESS_LEARNING.md`. Design a new flow (e.g. market data) with the repo-root `design-flow` skill before building it; this service's learning track stays in `../docs/go/`.

Read these before doing anything:

| File | What it is |
|---|---|
| `../docs/go/TASKS.md` | The plan: steps, tasks, "done when", current status |
| `../docs/go/LEARNING.md` | What Raguram learns in each step (Go + market-data concepts) |
| `../docs/go/CONVENTIONS.md` | Go naming, layout, branches, commit format |
| `../docs/go/DECISIONS.md` | Decisions (MD-xx) and open questions for this service |
| `../docs/go/EVENTS.md` | The event contract with the Java engine (draft) |
| `../docs/go/DEV_LOG.md` | Log of every change made to the project |
| `../docs/go/LEARNING_LOG.md` | Log of what Raguram learned in each step |

## Rule 1: never write code without approval (most important)

Raguram must approve every change before it is written. No exceptions, even for "small" changes.

1. **Explain** what you want to do and why, in simple English.
2. **Show** the exact code or change in the chat (full file, or a clear diff).
3. **Explain the new parts**: every new Go idea, with a Java comparison where it helps (`explain-go-for-java` skill).
4. **Ask**: "Approve? (yes / change something / no)".
5. **Wait.** Only a clear "yes" / "approved" / "go ahead" counts. A question back is NOT approval.
6. Only then write the file, then update `../docs/go/DEV_LOG.md`.

This applies to: `.go` files, `go.mod` / `go.sum`, `go get` / `go mod tidy`, config, Dockerfile, deletes, renames and git commits.
Read-only work (reading, explaining, `go version`, `go vet`, `go test`, `go build` of approved code) does not need approval.

If Raguram types the code himself (preferred for new ideas), review it and explain. Don't rewrite it unless he asks.

## Rule 2: teach while building

- Before each step: explain only the Go concepts that step needs (`../docs/go/LEARNING.md` row), 10–15 min, with 1–2 check questions.
- One small piece at a time: aim for under ~30 new lines per change.
- Show **why Go does it this way**, not only how. Point out where the Java habit would be wrong (no classes, no exceptions, no DI container, no annotations).
- After each step, Raguram explains it back in his own words → `../docs/go/LEARNING_LOG.md`.
- If he approves something he can't explain, stop and explain.

## Rule 3: keep the logs

- Every approved change → one entry in `../docs/go/DEV_LOG.md` (`project-logs` skill).
- Every finished step → one entry in `../docs/go/LEARNING_LOG.md`.
- New Go vs Java items → quick-reference table in `../docs/go/LEARNING.md`.
- Every commit → format in `../docs/go/CONVENTIONS.md`.
- Update the status column in `../docs/go/TASKS.md`.

## Rule 4: ZetaX hard rules (same as the Java side)

- Prices are `int64` **ticks**, quantities `int64`. **Never `float64` for money or quantity.** Convert to display only in the UI.
- Time comes from the **event** (Kafka record timestamp / event field), never `time.Now()`, for anything that affects state (candles, book). Wall clock is only for logs and metrics.
- Events can arrive **twice** (at-least-once). Handling must be idempotent, keyed on `eventId`.
- Per symbol, events must be applied **in order**. Never process one symbol's events on two goroutines at once.
- This service is **read-only** towards ZetaX: it never produces to `commands`, never changes orders.
- WebSocket: snapshot first, then deltas with a per-symbol sequence number; client refetches on a gap.
- Ask before adding **any** dependency (`go get`). Prefer the standard library first.

## Rule 5: Go quality bar

- `gofmt` (VS Code does it on save), `go vet ./...` and `go test -race ./...` pass before any commit.
- Every error is handled or returned with context (`fmt.Errorf("decode event: %w", err)`). No `_` for errors without a comment saying why.
- Every goroutine has a clear owner and a way to stop (`context.Context`). No goroutine leaks.
- `context.Context` is the first parameter of anything that blocks or does I/O.
- Small interfaces, defined where they are **used**. Accept interfaces, return structs.
- Use the `go-review` skill before proposing a commit.

## Tech

- Go (current stable; Raguram installs with `brew install go`, check with `go version`).
- Module path: `github.com/raguram-24/zetax-exchange/zetax-marketdata` (confirm at Step 0). Own `go.mod` in this folder; it is **not** a Maven module.
- Standard library first: `net/http`, `encoding/json`, `log/slog`, `testing`.
- To be chosen at the step that needs them (see `../docs/go/DECISIONS.md`): Kafka client, WebSocket library, Postgres driver, metrics library.
- Port: `:8082` (gateway 8080, engine 8081). Local Kafka + Postgres come from the repo-root `../docker-compose.yml`.
- Config from environment variables (`.env.example` lists them). No secrets in code.

## Skills in this project (`.claude/skills/`)

| Skill | Use when |
|---|---|
| `approval-gate` | Before ANY change to a file, `go get`, config, or commit |
| `start-build-step` | Raguram says "start step N", "next step", "what's next" |
| `explain-go-for-java` | Explaining any Go code or concept |
| `project-logs` | Writing DEV_LOG / LEARNING_LOG entries and commit messages |
| `handle-new-event` | Adding or changing an engine event type the service consumes |
| `go-review` | Reviewing Go code (yours or Raguram's) before approval or commit |

## Second Brain

Project notes may also go to Raguram's Obsidian vault (`~/Documents/Second_Brain/`), but **don't edit the vault from Claude Code unless Raguram asks**. Never touch `raw/` in the vault.
