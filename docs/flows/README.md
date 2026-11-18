# Flows — final diagrams (Java side)

The repo is the **source of truth** for flows; the vault is planning only (decided 2026-11-17).
Rule: **one flow = one document = one diagram**, added in the step that builds the flow.
The order lifecycle has its own document and grows a version per flow.

| Document | Flow | Step | Command → events | Status | Issue / branch |
|---|---|---|---|---|---|
| [ORDER-LIFECYCLE.md](ORDER-LIFECYCLE.md) | Order states (all flows) | grows per flow | — | v0 (BF-02) | — |
| [BF-02-place-limit-order.md](BF-02-place-limit-order.md) | BF-02 · Place a limit order (full flow: gateway → engine → persistor) | first flow (J1–J10) | `PlaceOrder` → `OrderAccepted`, `TradeExecuted` × n | **designed** 2026-11-18 (22 decisions) | ZTX-? · `feature/ZTX-?/place-limit-order` |
| (planned) | Safe retries: `clientOrderId` de-duplication (decision 18 in BF-02) | after BF-02 | — | planned, must be built and learned | — |
