# ZetaX Web

React + TypeScript (Vite) trading screen for **ZetaX Exchange** (D-10). Part of the [`zetax-exchange`](../README.md) repo.
Talks to `zetax-gateway`: REST for place/cancel (D-14), STOMP WebSocket for live book, trades and my orders.

🚧 Skeleton: layout and placeholders only. Built in phase 3b, after the gateway's WebSocket API works.

## Layout
```
Symbols    | Price chart    | Order form
Order book | Recent trades  | My orders
```

## Run
Requirements: Node 20+ (22 recommended).

```bash
npm install
cp .env.example .env.local     # set VITE_USER_ID (no login in v1, D-16)
npm run dev                    # http://localhost:5173
```
The dev server proxies `/api`, `/admin` and `/ws` to the gateway on `localhost:8080`.

Other scripts: `npm run build` · `npm run lint` · `npm run preview`
