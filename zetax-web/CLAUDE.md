# ZetaX Web — rules for Claude Code

## What this is
The React trading screen for ZetaX (an order matching engine). Lives **inside the `zetax-exchange` repo** as `zetax-web/`, next to `../zetax-gateway` (REST) and `../zetax-marketdata` (Go market-data service).
The repo-root `../CLAUDE.md` also applies for ZetaX-wide rules; its Java/Maven parts don't apply here.

> ⚠️ Pending decision D-22: live market data (book, trades, my orders) may move from the gateway's STOMP WebSocket to the Go `zetax-marketdata` service (protocol still open: MD-Q02 in `../docs/go/DECISIONS.md`). Don't build the WebSocket client until that is decided.
Design and decisions live in Raguram's vault: `~/Documents/Second_Brain/wiki/projects/order-matching-engine/`
(Overview = decisions D-01…D-21; "Business and Technical Flow" = BF-01 screen, TF-04 WebSocket, message shapes in B2).
Never invent requirements; ask if something isn't decided.

## Stack
React 19 + TypeScript + Vite. Plain CSS (variables in `src/index.css`). Lint: oxlint.
**Ask before adding any dependency** (STOMP client and chart library are still to be chosen).

## Structure
- `src/components/` one component per panel (SymbolList, PriceChart, OrderForm, OrderBook, RecentTrades, MyOrders)
- `src/api/` REST calls to the gateway (`/api`, `/admin`), always with `X-User-Id` (D-16)
- `src/ws/` STOMP WebSocket client: `/topic/book.{symbol}`, `/topic/trades.{symbol}`, `/user/queue/orders`
- `src/types/` types mirroring `zetax-common` (enums, Order, Trade, book levels)
- `src/config/` env (VITE_USER_ID)

## Rules
- Prices and quantities from the API are integers (ticks / lots). Convert to display values only in the UI, using the symbol's tick size. Never do money math with floats.
- REST place/cancel returns `202 Accepted`; show "pending" and update from WebSocket events (D-14).
- Book: apply the snapshot, then deltas in sequence; on a gap, refetch the snapshot.
- Buy = `--buy` (green), Sell = `--sell` (red).
- Keep components small and typed; no `any`.

## Done means
`npm run build` and `npm run lint` pass.
