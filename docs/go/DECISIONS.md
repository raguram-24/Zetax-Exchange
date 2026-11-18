# DECISIONS — market-data service

Decisions for this Go service only (`MD-xx`). ZetaX-wide decisions (`D-xx`) live in the vault: `Order Matching Engine - Overview.md`.
Raguram decides; Claude Code explains options and trade-offs. Write the reason in Raguram's words.

## Decisions

| ID | Date | Decision | Why | Alternatives considered |
|---|---|---|---|---|
| MD-01 | 2026-11-10 | Build the ZetaX **market-data service in Go** as a separate service (`zetax-marketdata`). The engine stays in Java. | Learn Go by building a real component; market data uses what Go is best at (goroutines, channels, fan-out over WebSocket). The engine's single-threaded hot loop wouldn't get faster in Go, and it stays the Java CV core. Talks to ZetaX only through Kafka (D-21 idea). | Engine in Go; demo bot / load generator in Go (kept as optional Step 14) |
| MD-02 | 2026-11-10 | **Learn while building** with Claude Code: no lessons first; approval before every change; DEV_LOG + LEARNING_LOG. | Same method as python-ai-agent; concepts stick when seen in my own code. | Read Go lessons first |
| MD-03 | 2026-11-10 | Steps 0–8 run on a **fake event source**; Kafka arrives in Step 9 behind the same interface. | The Java engine isn't built yet; the Go service must not wait for it. Also gives fast, deterministic tests. | Start with Kafka |
| MD-04 | 2026-11-10 | The service lives **inside the `zetax-exchange` repo** as the folder `zetax-marketdata/` (own `go.mod`, not a Maven module). Still a separate app (own process, port 8082). | One clone and one docker compose for the whole exchange; event contract next to `zetax-common`, so Java + Go contract changes go in one commit; shows the whole system in one repo. | Separate `zetax-marketdata` repo |

## ⚠️ ZetaX-level change to record in the vault (not done yet)

| Proposed | Change | Affects |
|---|---|---|
| D-22 | The market-data publisher moves **out of `zetax-gateway`** into this Go service. Gateway keeps REST orders + admin + Postgres writer. root `CLAUDE.md` + `README.md` (gateway `marketdata` package), `zetax-web/CLAUDE.md` (WebSocket URL / protocol), vault Overview (D-21, B1 diagram, TF-04) |

Raguram decides when to record D-22 and update those files.

## Open questions

| ID | Question | Options | Decide at |
|---|---|---|---|
| MD-Q01 | WebSocket library | `github.com/coder/websocket`, `github.com/gorilla/websocket` (verify current status of both) | Step 7 |
| MD-Q02 | WebSocket protocol: plain JSON messages (`{"op":"subscribe","channel":"book.ABC"}`) or STOMP like the Spring plan? | Plain JSON is simple in Go; STOMP matches the current `zetax-web` plan (web repo has no STOMP client yet) | Step 7 (agree with `zetax-web`) |
| MD-Q03 | Kafka client | `github.com/twmb/franz-go` (pure Go), `github.com/segmentio/kafka-go` (pure Go), `confluent-kafka-go` (cgo, wraps librdkafka) — verify maintenance status | Step 9 |
| MD-Q04 | Rebuild state on restart | Replay `trades` from earliest offset (but retention is 7 days: GTC orders resting longer would be lost from the book view); periodic snapshots; ask the engine for a book snapshot event | Step 9 |
| MD-Q05 | Postgres driver + migrations | `pgx` (+ `database/sql` or native); migrations with `goose` / `golang-migrate`; queries with `sqlc` (maybe) | Step 11 |
| MD-Q06 | Own the candle table: separate schema (`marketdata.candles`) in the same `zetax` DB, or own DB? | Same DB, own schema is simplest; services must not share tables | Step 11 |
| MD-Q07 | Consumer: consumer group or `assign` all partitions? | Market data is rebuildable, so a group is fine; but a group splits partitions across instances, and every instance needs **all** symbols for WebSocket | Step 9 |
| MD-Q08 | REST paths for snapshots (`/api/marketdata/{symbol}/book` vs `/api/book/{symbol}` from TF-04) | Agree with `zetax-web` | Step 6 |
| MD-Q09 | Candle with no trades in a minute: skip, or flat candle (open=high=low=close=previous close, volume 0)? | Chart library may need one or the other | Step 4 |
