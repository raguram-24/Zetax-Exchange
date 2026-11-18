---
name: handle-new-event
description: Use when adding or changing an engine event type that the market-data service consumes from the Kafka trades topic (Step 1 onwards) - contract, sample, Go struct, decoder, state changes, tests.
---

# Handle a new (or changed) engine event

The event contract with the Java engine is `../docs/go/EVENTS.md`. Never invent fields. If the engine side doesn't have it yet, it's a question for Raguram, not a guess.

## Checklist (each code item goes through `approval-gate`)

1. **Contract first:** the event's `type` value and every field (name, JSON type, meaning, required?) are written in `../docs/go/EVENTS.md` and match `zetax-common` (or are marked as "to add on the Java side").
2. **Sample:** `testdata/events/<type>.json` with one realistic example (ticks, not decimals).
3. **Go struct:** exported fields, JSON tags in camelCase matching Java (`EventID string \`json:"eventId"\``), `int64` for prices and quantities. No `float64`.
4. **Decoder:** add the `type` to the switch; unknown types return a wrapped error (don't crash the consumer; decide log-and-skip vs stop with Raguram).
5. **Validation:** required fields present, qty > 0, price > 0 where it applies. Return errors, don't panic.
6. **State change:** what this event does to the book view, trades, candles and per-user streams. Write it as a short table first, then code.
7. **Idempotency:** applying the same `eventId` twice changes nothing.
8. **Ordering:** handled on the symbol's single owner goroutine only.
9. **Outbound:** which WebSocket channel gets a delta (`book.ABC`, `trades.ABC`, user stream) and the sequence number increases once per change.
10. **Tests:** decode test (good + bad JSON), state test (table-driven), duplicate test.
11. **Log:** DEV_LOG entry + update `../docs/go/TASKS.md`; if the contract changed, note it for the Java side.

## Explain to Raguram

- How the JSON maps to the struct (tags, zero values when a field is missing — a missing `qty` becomes `0`, which is why validation matters).
- Java comparison: struct + tags ≈ a Java `record` + Jackson `@JsonProperty`; the decoder switch ≈ Jackson `@JsonTypeInfo` polymorphism, but written by hand.
