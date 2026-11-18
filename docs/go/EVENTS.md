# EVENTS — contract with the Java engine (DRAFT)

The market-data service only **reads** the Kafka `trades` topic. This page is the contract between the Java engine (`../zetax-engine`, contracts in `../zetax-common`) and this Go service. Both live in the same repo, so contract changes go in **one commit** on both sides.
Java shares classes through `zetax-common`; Go can't use those, so **this written contract is the source of truth for Go**. Keep it in sync with `zetax-common`.

Source: vault → `Order Matching Engine - Business and Technical Flow.md` → **B2** (topics + message shapes, marked "💡 suggestion, v1 JSON" there).
Status: ⚠️ **draft — only the TRADE shape exists in the design.** Everything marked ❓ must be agreed (and added to the Java side) before Step 1 is finished. Don't invent fields: decide them with Raguram.

## Topic

| Topic | Key | Partitions | Retention | This service |
|---|---|---|---|---|
| `trades` | `symbol` | 3 (fixed, D-18) | 7 days (`docker-compose.yml`: `retention.ms=604800000`) | consumes all partitions |

- Order is guaranteed **per partition**, so per symbol (key = symbol).
- Delivery is **at-least-once**: the same event may arrive twice → dedupe on `eventId`.
- `eventId` = `partition-offset-n` of the **command** that produced it (deterministic, same after engine replay).

## Event types (from B2)

| Type | Meaning | Shape known? |
|---|---|---|
| `OrderAccepted` | Order passed validation and entered the engine (may then rest in the book) | ❓ no |
| `OrderRejected` | Order refused (bad price band, qty limits, `NO_LIQUIDITY` for market orders…) | ❓ no |
| `TradeExecuted` | A match between a resting maker and an incoming taker | ✅ draft below |
| `OrderCancelled` | Remaining qty removed from the book (user cancel, IOC remainder, self-trade prevention) | ❓ no |
| `CancelRejected` | Cancel refused (e.g. already filled) | ❓ no |

### `TradeExecuted` (draft from B2)

```json
{ "type": "TRADE", "eventId": "2-1532-1", "symbol": "ABC", "tradeId": "2-1532-1",
  "priceTicks": 10100, "qty": 30, "makerOrderId": "…carol", "takerOrderId": "…alice",
  "makerUserId": "carol", "takerUserId": "alice", "sourceOffset": 1532 }
```

Note: B2 uses the type value `"TRADE"` in JSON but the name `TradeExecuted` in the table. ❓ Agree the exact `type` strings.

## Open questions (must be answered before / during Step 1)

| ID | Question | Why market data needs it |
|---|---|---|
| EV-Q01 | Exact `type` values for all 5 events (`TRADE` vs `TRADE_EXECUTED`…)? | Decoder switches on it |
| EV-Q02 | Does `OrderAccepted` carry `side`, `priceTicks`, `orderType` and the qty that **rests** in the book after immediate fills? | To add the resting qty to the right price level |
| EV-Q03 | Does `TradeExecuted` say the maker's **side** (or price level)? Or must market data remember every resting order by `orderId`? | To reduce the right level. Option A: events carry enough info (stateless levels). Option B: market data keeps `orderId → side, price, remaining` (more memory, more logic, but fewer fields) |
| EV-Q04 | Does `OrderCancelled` carry `side`, `priceTicks` and the cancelled qty? | To remove qty from the level |
| EV-Q05 | Event time: a field in the JSON (e.g. `ts` in ms) or only the Kafka record timestamp? | Candles are bucketed by event time |
| EV-Q06 | Modify (D-13: reduce qty keeps priority, price change loses it): own event type, or cancel + accept? | Book level changes |
| EV-Q07 | Symbol admin events (D-20: add / halt / resume / delist): do they appear on `trades`, so market data knows the symbol list and tick sizes? | Snapshot of an unknown symbol; halted status on screen |
| EV-Q08 | Order updates per user (Step 10): are `OrderAccepted` / `OrderCancelled` / `OrderRejected` enough to show "my orders", including fill progress? | Per-user stream |
| EV-Q09 | Versioning: add a `"v": 1` field? | Safe changes later |

## Samples

Once agreed, each event gets one sample file in `testdata/events/<type>.json` (Step 1). The same samples can be produced to Kafka by hand in Step 9 to test without the engine.
