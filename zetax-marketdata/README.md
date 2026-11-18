# ZetaX Market Data

> The live market-data service for **ZetaX**, a Kafka-backed order matching engine. Written in **Go**.

🚧 Work in progress (portfolio + Go learning project).

ZetaX's matching engine (Java + Spring Boot) publishes every order and trade event to Kafka.
This service reads those events and serves the **market view** to the trading screen:

- **Order book by price level** (total quantity per price, bids and asks)
- **Recent trades** and **OHLC candles**
- **WebSocket** stream: snapshot first, then deltas with sequence numbers
- **REST** for snapshots and candle history

```
zetax-engine (Java) ──► Kafka "trades" ──► zetax-marketdata (Go) ──WebSocket / REST──► zetax-web (React)
```

Part of the [`zetax-exchange`](../README.md) repo, next to the Java modules `zetax-common`, `zetax-engine` and `zetax-gateway`. Frontend: `zetax-web` (React screen).

## Status

See [`../docs/go/TASKS.md`](../docs/go/TASKS.md). Not runnable yet.

## Docs

| File | What |
|---|---|
| [`../docs/go/TASKS.md`](../docs/go/TASKS.md) | Build plan, step by step |
| [`../docs/go/EVENTS.md`](../docs/go/EVENTS.md) | Event contract with the engine |
| [`../docs/go/DECISIONS.md`](../docs/go/DECISIONS.md) | Design decisions and open questions |
| [`../docs/go/CONVENTIONS.md`](../docs/go/CONVENTIONS.md) | Naming, layout, commits |
| [`../docs/go/LEARNING.md`](../docs/go/LEARNING.md) | What each step teaches about Go |
