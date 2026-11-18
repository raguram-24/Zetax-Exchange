# Order lifecycle

Grows one version per flow. Back to the [flow index](README.md).

| Version | Flow | Added |
|---|---|---|
| v0 | [BF-02](BF-02-place-limit-order.md) place limit | RECEIVED, OPEN, PARTIALLY_FILLED, FILLED |

```mermaid
stateDiagram-v2
    [*] --> RECEIVED: gateway journaled PlaceOrder (202)
    RECEIVED --> OPEN: engine - no match, rests
    RECEIVED --> PARTIALLY_FILLED: engine - some traded, rest rests
    RECEIVED --> FILLED: engine - all traded
    OPEN --> PARTIALLY_FILLED: a taker trades part
    OPEN --> FILLED: a taker trades all
    PARTIALLY_FILLED --> FILLED: rest traded
    FILLED --> [*]
```
- RECEIVED is set by the gateway (the `202` answer); every later state comes from engine events.
- An order that trades on arrival skips OPEN.
- A resting (maker) order changes state because of someone else's command.
